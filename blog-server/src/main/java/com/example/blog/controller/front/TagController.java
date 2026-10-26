package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.TagService;
import com.example.blog.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontTagController")
@RequestMapping("/api/front/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<TagVO>> list() {
        return Result.ok(tagService.list());
    }
}
