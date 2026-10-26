package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.dto.CommentPostRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.CommentService;
import com.example.blog.util.RateLimitUtil;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int RATE_LIMIT_PER_MINUTE = 5;

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final RateLimitUtil rateLimitUtil;

    @Override
    public List<CommentVO> tree(Long articleId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime).orderByAsc(Comment::getId));
        Map<Long, CommentVO> voMap = new LinkedHashMap<>();
        List<CommentVO> tops = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = toVO(c);
            vo.setReplies(new ArrayList<>());
            voMap.put(c.getId(), vo);
            CommentVO parent = c.getParentId() == null ? null : voMap.get(c.getParentId());
            if (parent == null) {
                tops.add(vo);
            } else {
                parent.getReplies().add(vo);
            }
        }
        return tops;
    }

    @Override
    public void post(Long articleId, CommentPostRequest request, String ip) {
        rateLimitUtil.check("comment", ip, RATE_LIMIT_PER_MINUTE);
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文章不存在");
        }
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setParentId(normalizeParentId(articleId, request.getParentId()));
        comment.setNickname(request.getNickname().trim());
        comment.setEmail(orDefault(request.getEmail()));
        comment.setContent(request.getContent().trim());
        comment.setStatus(1);
        comment.setIp(ip);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        articleMapper.update(null, new UpdateWrapper<Article>().eq("id", articleId)
                .setSql("comment_count = comment_count + 1"));
    }

    /**
     * 仅支持两级：回复楼中楼时归一化挂到顶级评论下
     */
    private Long normalizeParentId(Long articleId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Comment parent = commentMapper.selectById(parentId);
        if (parent == null || !Objects.equals(parent.getArticleId(), articleId) || parent.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "回复的评论不存在或已隐藏");
        }
        return parent.getParentId() != null ? parent.getParentId() : parent.getId();
    }

    @Override
    public PageResult<AdminCommentVO> pageAdmin(long pageNum, long pageSize, String articleTitle, String nickname) {
        LambdaQueryWrapper<Comment> w = new LambdaQueryWrapper<Comment>()
                .like(StringUtils.hasText(nickname), Comment::getNickname, nickname)
                .orderByDesc(Comment::getCreateTime).orderByDesc(Comment::getId);
        if (StringUtils.hasText(articleTitle)) {
            Set<Long> articleIds = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                            .like(Article::getTitle, articleTitle).select(Article::getId))
                    .stream().map(Article::getId).collect(Collectors.toSet());
            if (articleIds.isEmpty()) {
                return new PageResult<>(List.of(), 0, pageNum, pageSize);
            }
            w.in(Comment::getArticleId, articleIds);
        }
        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return new PageResult<>(toAdminVos(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值不合法");
        }
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        if (comment.getStatus().equals(status)) {
            return;
        }
        commentMapper.update(null, new UpdateWrapper<Comment>().eq("id", id).set("status", status));
        int delta = status == 1 ? 1 : -1;
        articleMapper.update(null, new UpdateWrapper<Article>().eq("id", comment.getArticleId())
                .setSql("comment_count = GREATEST(comment_count + " + delta + ", 0)"));
    }

    @Override
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        commentMapper.deleteById(id);
        if (comment.getStatus() == 1) {
            articleMapper.update(null, new UpdateWrapper<Article>().eq("id", comment.getArticleId())
                    .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
        }
    }

    @Override
    public List<AdminCommentVO> latest(int limit) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .orderByDesc(Comment::getCreateTime).orderByDesc(Comment::getId)
                .last("LIMIT " + limit));
        return toAdminVos(comments);
    }

    private List<AdminCommentVO> toAdminVos(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> titles = articleIds.isEmpty() ? Map.of()
                : articleMapper.selectBatchIds(articleIds).stream()
                        .collect(Collectors.toMap(Article::getId, Article::getTitle));
        return comments.stream().map(c -> {
            AdminCommentVO vo = new AdminCommentVO();
            vo.setId(c.getId());
            vo.setArticleId(c.getArticleId());
            vo.setArticleTitle(c.getArticleId() == null ? null : titles.get(c.getArticleId()));
            vo.setNickname(c.getNickname());
            vo.setContent(c.getContent());
            vo.setStatus(c.getStatus());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).toList();
    }

    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setNickname(c.getNickname());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }

    private String orDefault(String s) {
        return s == null ? "" : s;
    }
}
