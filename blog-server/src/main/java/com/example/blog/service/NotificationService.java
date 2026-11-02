package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.vo.NotificationVO;

/**
 * 站内通知（v2.2）：评论/留言通知博主或文章作者（不发邮件）
 */
public interface NotificationService {

    /** 写入一条站内通知（内容自动截断；失败仅记日志，不影响调用方主流程） */
    void push(Long userId, String type, String title, String content, String link);

    /** 我的通知分页（按时间倒序） */
    PageResult<NotificationVO> pageMine(long pageNum, long pageSize);

    /** 我的未读数 */
    long unreadCount();

    /** 单条标记已读（校验归属） */
    void markRead(Long id);

    /** 全部已读 */
    void markAllRead();
}
