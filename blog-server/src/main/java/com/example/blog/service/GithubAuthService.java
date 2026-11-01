package com.example.blog.service;

import com.example.blog.dto.GithubCallbackRequest;
import com.example.blog.vo.FrontLoginVO;

/**
 * GitHub OAuth 登录（评审 §4.1）：授权页 URL 生成 + 回调换取登录态
 */
public interface GithubAuthService {

    /** 生成 GitHub 授权页跳转 URL（含一次性 state） */
    String authorizeUrl(String ip);

    /** 回调换取登录态：校验 state → 换 token → 拉用户 → 绑定/自动注册 → 登录 */
    FrontLoginVO login(GithubCallbackRequest request);
}
