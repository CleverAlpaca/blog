package com.example.blog.controller.front;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.dto.MyArticleSaveRequest;
import com.example.blog.service.ArticleService;
import com.example.blog.storage.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** 我的投稿（均需登录，/api/front/my/** 拦截器强制） */
@RestController
@RequestMapping("/api/front/my")
@RequiredArgsConstructor
public class MyArticleController {

    private final ArticleService articleService;
    private final FileStorageService fileStorageService;

    @PostMapping("/articles")
    public Result<Long> create(@Valid @RequestBody MyArticleSaveRequest body) {
        return Result.ok(articleService.createMine(body));
    }

    @GetMapping("/articles")
    public Result<PageResult<ArticleCardVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(articleService.pageMine(pageNum, Math.min(pageSize, 50), status));
    }

    @GetMapping("/articles/stats")
    public Result<Map<String, Long>> stats() {
        return Result.ok(articleService.statsMine());
    }

    @GetMapping("/articles/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.ok(articleService.getMineForEdit(id));
    }

    @PutMapping("/articles/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MyArticleSaveRequest body) {
        articleService.updateMine(id, body);
        return Result.ok();
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteMine(id);
        return Result.ok();
    }

    /** 投稿插图上传（登录用户，与后台同一存储实现） */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(Map.of("url", fileStorageService.upload(file)));
    }
}
