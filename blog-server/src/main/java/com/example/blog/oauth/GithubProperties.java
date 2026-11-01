package com.example.blog.oauth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GitHub OAuth 配置：blog.oauth.github.*
 * client-id/secret 为空时，authorize 端点返回明确错误（前台 GitHub 入口不可用）
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.oauth.github")
public class GithubProperties {

    private String clientId = "";

    private String clientSecret = "";

    /** GitHub 授权后回跳的前台落地路由（完整 URL） */
    private String redirectUri = "http://localhost:5173/auth/github";
}
