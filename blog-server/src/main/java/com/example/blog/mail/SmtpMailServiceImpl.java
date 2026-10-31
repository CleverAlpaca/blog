package com.example.blog.mail;

import com.example.blog.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 真实 SMTP 发信（blog.mail.type=smtp 时生效）；
 * JavaMailSender 由 spring.mail.host 配置驱动自动装配，用 ObjectProvider 延迟解析
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blog.mail.type", havingValue = "smtp")
public class SmtpMailServiceImpl implements MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final BlogMailProperties properties;

    @Override
    public void send(String to, String subject, String content) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new BusinessException(500, "邮件服务未配置（缺少 spring.mail.host），无法发送邮件");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        sender.send(message);
        log.info("邮件已发送 to={} subject={}", to, subject);
    }
}
