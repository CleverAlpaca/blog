package com.example.blog.service;

/**
 * 站内事件邮件通知（v2.1：仅投稿审核结果，见消息通知与GitHub登录设计评审 §4.2）
 */
public interface NotifyService {

    /** 审核结果通知投稿作者（发送失败仅记日志，不影响审核主流程） */
    void notifyReviewResult(Long articleId, boolean approved, String rejectReason);
}
