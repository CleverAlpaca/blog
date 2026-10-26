package com.example.blog.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.example.blog.common.Result;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.service.UserService;
import com.example.blog.vo.LoginVO;
import com.example.blog.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.ok(userService.profile(StpUtil.getLoginIdAsLong()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(StpUtil.getLoginIdAsLong(), request);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(StpUtil.getLoginIdAsLong(), request);
        return Result.ok();
    }
}
