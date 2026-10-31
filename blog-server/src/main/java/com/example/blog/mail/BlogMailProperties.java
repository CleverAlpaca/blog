package com.example.blog.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置：blog.mail.type = log（默认，日志模拟发信）| smtp（真实 SMTP）
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.mail")
public class BlogMailProperties {

    /** 发件人（SMTP 场景需与账号一致，如 no-reply@example.com） */
    private String from = "blog@localhost";
}
