package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/site-config")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping
    public Result<Map<String, String>> get() {
        return Result.ok(siteConfigService.getAllMap());
    }

    @PutMapping
    public Result<Void> update(@org.springframework.web.bind.annotation.RequestBody Map<String, String> configs) {
        siteConfigService.updateBatch(configs);
        return Result.ok();
    }
}
