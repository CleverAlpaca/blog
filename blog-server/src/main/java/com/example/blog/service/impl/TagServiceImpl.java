package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.TagSaveRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Tag;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.TagService;
import com.example.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<TagVO> list() {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));
        if (tags.isEmpty()) {
            return List.of();
        }
        // 关联表计数，再过滤出已发布文章
        List<ArticleTag> relations = articleTagMapper.selectList(null);
        if (relations.isEmpty()) {
            return tags.stream()
                    .map(t -> new TagVO(t.getId(), t.getName(), 0L))
                    .toList();
        }
        Set<Long> articleIds = relations.stream().map(ArticleTag::getArticleId).collect(Collectors.toSet());
        Set<Long> publishedIds = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds).select(Article::getId, Article::getStatus))
                .stream().filter(a -> a.getStatus() == 1)
                .map(Article::getId).collect(Collectors.toSet());
        Map<Long, Long> counts = relations.stream()
                .filter(r -> publishedIds.contains(r.getArticleId()))
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));
        return tags.stream()
                .map(t -> new TagVO(t.getId(), t.getName(), counts.getOrDefault(t.getId(), 0L)))
                .toList();
    }

    @Override
    public void save(TagSaveRequest request) {
        checkNameUnique(request.getName(), null);
        Tag tag = new Tag();
        tag.setName(request.getName());
        tagMapper.insert(tag);
    }

    @Override
    public void update(Long id, TagSaveRequest request) {
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "标签不存在");
        }
        checkNameUnique(request.getName(), id);
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(request.getName());
        tagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "标签不存在");
        }
        Long used = articleTagMapper.selectCount(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        if (used > 0) {
            throw new BusinessException(400, "该标签仍被文章使用，无法删除");
        }
        tagMapper.deleteById(id);
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Tag> w = new LambdaQueryWrapper<Tag>().eq(Tag::getName, name);
        if (excludeId != null) {
            w.ne(Tag::getId, excludeId);
        }
        if (tagMapper.selectCount(w) > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }
    }
}
