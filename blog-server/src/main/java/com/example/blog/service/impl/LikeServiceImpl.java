package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.entity.Article;
import com.example.blog.entity.LikeRecord;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.LikeRecordMapper;
import com.example.blog.service.LikeService;
import com.example.blog.vo.LikeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private static final int TYPE_ARTICLE = 1;

    private final LikeRecordMapper likeRecordMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public LikeVO toggle(Long articleId, String visitorId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文章不存在");
        }
        LambdaQueryWrapper<LikeRecord> w = new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getTargetType, TYPE_ARTICLE)
                .eq(LikeRecord::getTargetId, articleId)
                .eq(LikeRecord::getVisitorId, visitorId);
        LikeRecord exist = likeRecordMapper.selectOne(w);

        boolean liked;
        long likes;
        if (exist != null) {
            likeRecordMapper.deleteById(exist.getId());
            liked = false;
            likes = Math.max(0, article.getLikes() - 1);
        } else {
            LikeRecord record = new LikeRecord();
            record.setTargetType(TYPE_ARTICLE);
            record.setTargetId(articleId);
            record.setVisitorId(visitorId);
            record.setCreateTime(LocalDateTime.now());
            try {
                likeRecordMapper.insert(record);
                liked = true;
            } catch (DuplicateKeyException e) {
                // 并发下唯一键兜底：视为已点赞，不再重复计数
                log.debug("重复点赞被唯一键拦截, articleId={}, visitorId={}", articleId, visitorId);
                liked = true;
            }
            likes = article.getLikes() + 1;
        }
        articleMapper.update(null, new UpdateWrapper<Article>().eq("id", articleId).set("likes", likes));
        return new LikeVO(liked, likes);
    }
}
