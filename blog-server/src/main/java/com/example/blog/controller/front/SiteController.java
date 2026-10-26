package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.SiteService;
import com.example.blog.vo.AboutVO;
import com.example.blog.vo.SiteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping("/about")
    public Result<AboutVO> about() {
        return Result.ok(siteService.about());
    }

    @GetMapping("/stats")
    public Result<SiteStatsVO> stats() {
        return Result.ok(siteService.stats());
    }
}
