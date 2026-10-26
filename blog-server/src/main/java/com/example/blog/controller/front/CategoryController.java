package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.CategoryService;
import com.example.blog.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontCategoryController")
@RequestMapping("/api/front/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.ok(categoryService.list());
    }
}
