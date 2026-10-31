package com.example.blog.service;

import com.example.blog.dto.EmailBindRequest;
import com.example.blog.dto.ForgotPasswordRequest;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.vo.FrontLoginVO;
import com.example.blog.vo.UserVO;

public interface AuthService {

    /** 注册（同 IP 每小时限 5 次，邮箱验证码校验）并自动登录 */
    FrontLoginVO register(RegisterRequest request, String ip);

    /** 统一登录（ADMIN/USER 共用，封禁账号拒绝） */
    FrontLoginVO login(LoginRequest request);

    void logout();

    UserVO profile();

    void updateProfile(ProfileUpdateRequest request);

    void updatePassword(PasswordUpdateRequest request);

    /** 找回密码（邮箱验证码；成功后踢掉该账号全部会话；防枚举） */
    void forgotPassword(ForgotPasswordRequest request);

    /** 已登录换绑邮箱（验证新邮箱验证码） */
    void updateEmail(EmailBindRequest request);
}
