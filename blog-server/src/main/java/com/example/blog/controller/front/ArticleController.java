package com.example.blog.controller.front;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.CommentPostRequest;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.service.LikeService;
import com.example.blog.util.IpUtil;
import com.example.blog.vo.ArchiveYearVO;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.CommentVO;
import com.example.blog.vo.LikeVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontArticleController")
@RequestMapping("/api/front/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final LikeService likeService;

    @GetMapping("/list")
    public Result<PageResult<ArticleCardVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return Result.ok(articleService.pageFront(pageNum, Math.min(pageSize, 50), categoryId, tagId, keyword));
    }

    @GetMapping("/hot")
    public Result<List<ArticleCardVO>> hot() {
        return Result.ok(articleService.hot());
    }

    @GetMapping("/archives")
    public Result<List<ArchiveYearVO>> archives() {
        return Result.ok(articleService.archives());
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Visitor-Id", required = false) String visitorId) {
        return Result.ok(articleService.detail(id, visitorId));
    }

    @PostMapping("/{id}/like")
    public Result<LikeVO> like(
            @PathVariable Long id,
            @RequestHeader(value = "X-Visitor-Id", required = false) String visitorId,
            HttpServletRequest request) {
        String vid = StringUtils.hasText(visitorId) ? visitorId : "ip:" + IpUtil.getIp(request);
        return Result.ok(likeService.toggle(id, vid));
    }

    @GetMapping("/{id}/comment/list")
    public Result<List<CommentVO>> commentList(@PathVariable Long id) {
        return Result.ok(commentService.tree(id));
    }

    @PostMapping("/{id}/comment")
    public Result<Void> postComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentPostRequest body,
            HttpServletRequest request) {
        commentService.post(id, body, IpUtil.getIp(request));
        return Result.ok();
    }
}
