package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 我的通知列表项 */
@Data
public class NotificationVO {

    private Long id;

    /** 类型：comment 文章评论 / message 留言 */
    private String type;

    private String title;

    private String content;

    /** 点击跳转的站内路由 */
    private String link;

    /** 是否已读：0 未读 / 1 已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
