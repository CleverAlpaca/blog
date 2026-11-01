package com.example.blog.service.impl;

import com.example.blog.entity.Article;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.mail.MailService;
import com.example.blog.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 事件邮件通知实现。
 * 统一约束（评审 §4.2）：收件人开启通知开关、已绑定可收信邮箱、非本人操作；失败仅记日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final MailService mailService;

    @Value("${blog.site-url}")
    private String siteUrl;

    @Override
    public void notifyReviewResult(Long articleId, boolean approved, String rejectReason) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null || article.getUserId() == null) {
                return;
            }
            User author = userMapper.selectById(article.getUserId());
            if (author == null
                    || author.getEmailNotify() == null
                    || author.getEmailNotify() != 1
                    || author.getEmail() == null
                    || author.getEmail().isBlank()) {
                return;
            }
            String subject;
            String content;
            if (approved) {
                subject = "【个人博客】你投稿的《" + article.getTitle() + "》已通过审核";
                content = "你投稿的《" + article.getTitle() + "》已通过审核并发布。"
                        + "\n\n查看文章：" + siteUrl + "/article/" + article.getId();
            } else {
                subject = "【个人博客】你投稿的《" + article.getTitle() + "》已被退回";
                content = "你投稿的《" + article.getTitle() + "》未通过审核，退回原因："
                        + (rejectReason == null || rejectReason.isBlank() ? "未填写" : rejectReason)
                        + "\n\n修改后可重新提交审核：" + siteUrl + "/my/articles";
            }
            mailService.send(author.getEmail(), subject, content);
            log.info("审核结果通知已发送 to={} articleId={} approved={}", author.getEmail(), articleId, approved);
        } catch (Exception e) {
            // 通知是旁路能力：任何失败都不能影响审核主流程
            log.error("审核结果通知发送失败 articleId={}", articleId, e);
        }
    }
}
