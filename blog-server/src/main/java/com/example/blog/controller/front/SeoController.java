package com.example.blog.controller.front;

import com.example.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SEO 根路径端点：/robots.txt 与 /sitemap.xml 按惯例必须位于站点根路径，
 * 与 /api/front 下的同名接口共用 Service（生产 Nginx 直接转发根路径到这里）。
 */
@RestController
@RequiredArgsConstructor
public class SeoController {

    private final SiteService siteService;

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> robots() {
        return ResponseEntity.ok().body(siteService.robots());
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> sitemap() {
        return ResponseEntity.ok().body(siteService.sitemap());
    }
}
