package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.service.DashboardService;
import com.example.blog.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> dashboard() {
        return Result.ok(dashboardService.dashboard());
    }
}
