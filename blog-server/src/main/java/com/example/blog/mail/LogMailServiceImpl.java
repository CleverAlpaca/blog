package com.example.blog.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 日志模拟发信（默认）：验证码直接打到后端日志，便于无 SMTP 环境联调
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blog.mail.type", havingValue = "log", matchIfMissing = true)
public class LogMailServiceImpl implements MailService {

    private final BlogMailProperties properties;

    @Override
    public void send(String to, String subject, String content) {
        log.info("[MAIL-LOG-SIM] from={} to={} subject={} content={}", properties.getFrom(), to, subject, content);
    }
}
