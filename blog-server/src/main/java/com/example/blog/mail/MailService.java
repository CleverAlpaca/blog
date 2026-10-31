package com.example.blog.mail;

/**
 * 邮件发送抽象：log 实现打日志（开发联调）/ smtp 实现走真实 SMTP（生产）
 */
public interface MailService {

    void send(String to, String subject, String content);
}
