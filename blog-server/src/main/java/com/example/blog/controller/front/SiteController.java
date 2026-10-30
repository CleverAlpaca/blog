package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.SiteService;
import com.example.blog.vo.AboutVO;
import com.example.blog.vo.SiteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    /** RSS 2.0 订阅源（返回原始 XML，不走统一响应包装） */
    @GetMapping("/rss.xml")
    public ResponseEntity<String> rss() {
        // String 返回值经内容协商会被写成 text/plain，这里显式指定 RSS 类型
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/rss+xml;charset=UTF-8"))
                .body(siteService.rss());
    }
}
