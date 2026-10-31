package com.example.blog.controller.front;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.service.SiteService;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.UserCommentVO;
import com.example.blog.vo.UserHomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/user")
@RequiredArgsConstructor
public class UserPageController {

    private final SiteService siteService;
    private final ArticleService articleService;
    private final CommentService commentService;

    /** 用户公开资料（封禁用户返回 404） */
    @GetMapping("/{id}")
    public Result<UserHomeVO> home(@PathVariable Long id) {
        return Result.ok(siteService.userHome(id));
    }

    /** 该用户已发布的文章分页 */
    @GetMapping("/{id}/articles")
    public Result<PageResult<ArticleCardVO>> articles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(articleService.pagePublishedByUser(id, pageNum, Math.min(pageSize, 50)));
    }

    /** 该用户的公开评论分页（仅显示中且文章已发布） */
    @GetMapping("/{id}/comments")
    public Result<PageResult<UserCommentVO>> comments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(commentService.pageByUser(id, pageNum, Math.min(pageSize, 50)));
    }
}
