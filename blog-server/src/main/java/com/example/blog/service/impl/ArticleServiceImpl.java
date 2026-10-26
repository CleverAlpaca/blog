package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleSaveRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.Category;
import com.example.blog.entity.Comment;
import com.example.blog.entity.LikeRecord;
import com.example.blog.entity.Tag;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.LikeRecordMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.ArchiveItemVO;
import com.example.blog.vo.ArchiveYearVO;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String VIEW_HASH_KEY = "blog:article:view";
    private static final int TYPE_ARTICLE = 1;

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<ArticleCardVO> pageFront(long pageNum, long pageSize, Long categoryId, Long tagId, String keyword) {
        LambdaQueryWrapper<Article> w = frontWrapper()
                .like(StringUtils.hasText(keyword), Article::getTitle, keyword)
                .eq(categoryId != null, Article::getCategoryId, categoryId);
        if (tagId != null) {
            List<Long> ids = articleIdsOfTag(tagId);
            if (ids.isEmpty()) {
                return new PageResult<>(List.of(), 0, pageNum, pageSize);
            }
            w.in(Article::getId, ids);
        }
        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return new PageResult<>(toCards(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ArticleDetailVO detail(Long id, String visitorId) {
        Article article = require(id, true);
        stringRedisTemplate.opsForHash().increment(VIEW_HASH_KEY, String.valueOf(id), 1);
        ArticleDetailVO vo = toDetail(article);
        fillNavigation(vo, article);
        boolean liked = false;
        if (StringUtils.hasText(visitorId)) {
            liked = likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                    .eq(LikeRecord::getTargetType, TYPE_ARTICLE)
                    .eq(LikeRecord::getTargetId, id)
                    .eq(LikeRecord::getVisitorId, visitorId)) > 0;
        }
        vo.setLiked(liked);
        return vo;
    }

    @Override
    public ArticleDetailVO getForEdit(Long id) {
        return toDetail(require(id, false));
    }

    @Override
    public List<ArticleCardVO> hot() {
        List<Article> list = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getViews).orderByDesc(Article::getId)
                .last("LIMIT 5"));
        return toCards(list);
    }

    @Override
    public List<ArchiveYearVO> archives() {
        List<Article> list = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime).orderByDesc(Article::getId)
                .select(Article::getId, Article::getTitle, Article::getCreateTime));
        Map<Integer, List<ArchiveItemVO>> byYear = new LinkedHashMap<>();
        for (Article a : list) {
            byYear.computeIfAbsent(a.getCreateTime().getYear(), k -> new ArrayList<>())
                    .add(new ArchiveItemVO(a.getId(), a.getTitle(), a.getCreateTime()));
        }
        return byYear.entrySet().stream()
                .map(e -> new ArchiveYearVO(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public PageResult<ArticleCardVO> pageAdmin(long pageNum, long pageSize, String keyword, Integer status, Long categoryId) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<Article>()
                .like(StringUtils.hasText(keyword), Article::getTitle, keyword)
                .eq(status != null, Article::getStatus, status)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .orderByDesc(Article::getCreateTime).orderByDesc(Article::getId);
        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return new PageResult<>(toCards(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public Long save(ArticleSaveRequest request) {
        Article article = new Article();
        article.setUserId(StpUtil.getLoginIdAsLong());
        applyRequest(article, request);
        articleMapper.insert(article);
        saveTags(article.getId(), request.getTagIds());
        return article.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ArticleSaveRequest request) {
        require(id, false);
        Article article = new Article();
        article.setId(id);
        applyRequest(article, request);
        articleMapper.updateById(article);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        saveTags(id, request.getTagIds());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        require(id, false);
        articleMapper.deleteById(id);
        commentMapper.update(null, new UpdateWrapper<Comment>().eq("article_id", id).set("status", 0));
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
    }

    @Override
    public void setTop(Long id, Integer isTop) {
        require(id, false);
        articleMapper.update(null, new UpdateWrapper<Article>()
                .eq("id", id)
                .set("is_top", isTop != null && isTop == 1 ? 1 : 0));
    }

    // ---------------------------------------------------------------

    private LambdaQueryWrapper<Article> frontWrapper() {
        return new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getCreateTime)
                .orderByDesc(Article::getId);
    }

    private List<Long> articleIdsOfTag(Long tagId) {
        return articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId))
                .stream().map(ArticleTag::getArticleId).distinct().toList();
    }

    private Article require(Long id, boolean publishedOnly) {
        Article article = articleMapper.selectById(id);
        if (article == null || (publishedOnly && article.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文章不存在");
        }
        return article;
    }

    private void applyRequest(Article article, ArticleSaveRequest request) {
        article.setTitle(request.getTitle());
        article.setCoverImage(orDefault(request.getCoverImage()));
        article.setCategoryId(request.getCategoryId());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        article.setIsTop(request.getIsTop() == null ? 0 : request.getIsTop());
        article.setSummary(StringUtils.hasText(request.getSummary())
                ? request.getSummary() : autoSummary(request.getContent()));
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Long> validIds = tagMapper.selectBatchIds(tagIds.stream().distinct().toList())
                .stream().map(Tag::getId).toList();
        for (Long tagId : validIds) {
            ArticleTag relation = new ArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tagId);
            articleTagMapper.insert(relation);
        }
    }

    private void fillNavigation(ArticleDetailVO vo, Article article) {
        Article prev = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .lt(Article::getCreateTime, article.getCreateTime())
                .orderByDesc(Article::getCreateTime).orderByDesc(Article::getId)
                .last("LIMIT 1")
                .select(Article::getId, Article::getTitle));
        if (prev != null) {
            vo.setPrevId(prev.getId());
            vo.setPrevTitle(prev.getTitle());
        }
        Article next = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .gt(Article::getCreateTime, article.getCreateTime())
                .orderByAsc(Article::getCreateTime).orderByAsc(Article::getId)
                .last("LIMIT 1")
                .select(Article::getId, Article::getTitle));
        if (next != null) {
            vo.setNextId(next.getId());
            vo.setNextTitle(next.getTitle());
        }
    }

    private ArticleDetailVO toDetail(Article a) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setSummary(a.getSummary());
        vo.setCoverImage(a.getCoverImage());
        vo.setCategoryId(a.getCategoryId());
        vo.setCategoryName(a.getCategoryId() == null ? null
                : Optional.ofNullable(categoryMapper.selectById(a.getCategoryId())).map(Category::getName).orElse(null));
        vo.setTags(tagsOf(a.getId()));
        vo.setContent(a.getContent());
        vo.setStatus(a.getStatus());
        vo.setIsTop(a.getIsTop());
        // DB 落库值 + Redis 未落库增量 = 实际浏览量
        Object buffered = stringRedisTemplate.opsForHash().get(VIEW_HASH_KEY, String.valueOf(a.getId()));
        long bufferedViews = buffered == null ? 0 : Long.parseLong(buffered.toString());
        vo.setViews(a.getViews() + bufferedViews);
        vo.setLikes(a.getLikes());
        vo.setCommentCount(a.getCommentCount());
        vo.setCreateTime(a.getCreateTime());
        vo.setUpdateTime(a.getUpdateTime());
        return vo;
    }

    private List<ArticleCardVO> toCards(List<Article> articles) {
        if (articles.isEmpty()) {
            return List.of();
        }
        List<Long> ids = articles.stream().map(Article::getId).toList();
        Set<Long> catIds = articles.stream().map(Article::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> catNames = catIds.isEmpty() ? Map.of()
                : categoryMapper.selectBatchIds(catIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, ids));
        Map<Long, List<Long>> tagIdsByArticle = relations.stream().collect(Collectors.groupingBy(
                ArticleTag::getArticleId, Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));
        Set<Long> tagIds = relations.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNames = tagIds.isEmpty() ? Map.of()
                : tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getName));
        return articles.stream().map(a -> {
            ArticleCardVO vo = new ArticleCardVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setSummary(a.getSummary());
            vo.setCoverImage(a.getCoverImage());
            vo.setStatus(a.getStatus());
            vo.setIsTop(a.getIsTop());
            vo.setCategoryId(a.getCategoryId());
            vo.setCategoryName(catNames.get(a.getCategoryId()));
            vo.setTags(tagIdsByArticle.getOrDefault(a.getId(), List.of()).stream()
                    .map(tid -> new TagVO(tid, tagNames.get(tid))).toList());
            vo.setViews(a.getViews());
            vo.setLikes(a.getLikes());
            vo.setCommentCount(a.getCommentCount());
            vo.setCreateTime(a.getCreateTime());
            return vo;
        }).toList();
    }

    private List<TagVO> tagsOf(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(
                        new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId))
                .stream().map(ArticleTag::getTagId).toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> names = tagMapper.selectBatchIds(new HashSet<>(tagIds)).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        return tagIds.stream().map(tid -> new TagVO(tid, names.get(tid))).toList();
    }

    private String autoSummary(String content) {
        String text = content
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[([^]]*)]\\([^)]*\\)", "$1")
                .replaceAll("[#>*`~|]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return text.length() > 100 ? text.substring(0, 100) : text;
    }

    private String orDefault(String s) {
        return s == null ? "" : s;
    }
}
