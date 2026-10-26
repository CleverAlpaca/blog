package com.example.blog.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.entity.Article;
import com.example.blog.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 浏览量定时落库：
 * 详情接口实时写 Redis HASH（field=articleId, value=增量），
 * 本任务每 10 分钟 RENAME 原子切换后批量累加到 article.views，保证不丢计数。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncTask {

    private static final String VIEW_HASH_KEY = "blog:article:view";

    private final StringRedisTemplate stringRedisTemplate;
    private final ArticleMapper articleMapper;

    @Scheduled(fixedDelay = 600_000, initialDelay = 60_000)
    public void syncToDatabase() {
        try {
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(VIEW_HASH_KEY))) {
                return;
            }
            String bakKey = VIEW_HASH_KEY + ":syncing";
            stringRedisTemplate.rename(VIEW_HASH_KEY, bakKey);
            Map<Object, Object> deltas = stringRedisTemplate.opsForHash().entries(bakKey);
            for (Map.Entry<Object, Object> entry : deltas.entrySet()) {
                long articleId = Long.parseLong(entry.getKey().toString());
                long delta = Long.parseLong(entry.getValue().toString());
                if (delta <= 0) {
                    continue;
                }
                articleMapper.update(null, new UpdateWrapper<Article>()
                        .eq("id", articleId)
                        .setSql("views = views + " + delta));
            }
            stringRedisTemplate.delete(bakKey);
            if (!deltas.isEmpty()) {
                log.info("浏览量落库完成，涉及文章数 {}", deltas.size());
            }
        } catch (Exception ex) {
            log.error("浏览量定时落库失败", ex);
        }
    }
}
