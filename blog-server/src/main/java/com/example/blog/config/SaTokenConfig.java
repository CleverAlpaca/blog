package com.example.blog.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截：/api/admin/** 需登录（login 接口除外），/api/front/** 放行
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理端：登录 + ADMIN 角色双重校验（注册用户的 token 会被拒绝）
        registry.addInterceptor(new SaInterceptor(handle -> {
                    StpUtil.checkLogin();
                    StpUtil.checkRole("ADMIN");
                }))
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");

        // 前台登录态端点：我的投稿 / 资料 / 改密 / 登出 / 换绑邮箱
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns(
                        "/api/front/my/**",
                        "/api/front/auth/profile",
                        "/api/front/auth/password",
                        "/api/front/auth/email",
                        "/api/front/auth/logout");
    }
}
