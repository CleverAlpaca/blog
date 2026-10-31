package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.dto.EmailBindRequest;
import com.example.blog.dto.EmailCodeSendRequest;
import com.example.blog.dto.ForgotPasswordRequest;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.service.AuthService;
import com.example.blog.service.EmailCodeService;
import com.example.blog.util.IpUtil;
import com.example.blog.vo.FrontLoginVO;
import com.example.blog.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台认证：注册 / 登录 / 登出 / 资料 / 改密 / 邮箱验证码 / 找回密码 / 换绑邮箱
 * （ADMIN 与 USER 共用，管理端另有专属接口）
 */
@RestController("frontAuthController")
@RequestMapping("/api/front/auth")
@RequiredArgsConstructor
public class FrontAuthController {

    private final AuthService authService;
    private final EmailCodeService emailCodeService;

    @PostMapping("/register")
    public Result<FrontLoginVO> register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request) {
        return Result.ok(authService.register(body, IpUtil.getIp(request)));
    }

    @PostMapping("/login")
    public Result<FrontLoginVO> login(@Valid @RequestBody LoginRequest body) {
        return Result.ok(authService.login(body));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.ok(authService.profile());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest body) {
        authService.updateProfile(body);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest body) {
        authService.updatePassword(body);
        return Result.ok();
    }

    /** 发送邮箱验证码（scene：register 注册 / reset 找回密码 / bind 换绑，bind 需登录） */
    @PostMapping("/send-email-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody EmailCodeSendRequest body, HttpServletRequest request) {
        emailCodeService.sendCode(body.getScene(), body.getEmail(), IpUtil.getIp(request));
        return Result.ok();
    }

    /** 找回密码：邮箱验证码 + 新密码（成功后旧会话全部失效） */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest body) {
        authService.forgotPassword(body);
        return Result.ok();
    }

    /** 已登录换绑邮箱（验证新邮箱验证码） */
    @PutMapping("/email")
    public Result<Void> updateEmail(@Valid @RequestBody EmailBindRequest body) {
        authService.updateEmail(body);
        return Result.ok();
    }
}
