package com.example.blog.controller.admin;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.ArticleSaveRequest;
import com.example.blog.service.ArticleService;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.ArticleDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/page")
    public Result<PageResult<ArticleCardVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId) {
        return Result.ok(articleService.pageAdmin(pageNum, Math.min(pageSize, 50), keyword, status, categoryId));
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.getForEdit(id));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody ArticleSaveRequest request) {
        return Result.ok(articleService.save(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleSaveRequest request) {
        articleService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/top")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam(defaultValue = "1") Integer isTop) {
        articleService.setTop(id, isTop);
        return Result.ok();
    }
}
