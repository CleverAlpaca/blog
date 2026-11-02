package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 站内通知（v2.2：评论/留言通知博主或文章作者） */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收件人用户ID */
    private Long userId;

    /** 类型：comment 文章评论 / message 留言 */
    private String type;

    private String title;

    private String content;

    /** 点击跳转的站内路由 */
    private String link;

    /** 是否已读：0 未读 / 1 已读 */
    private Integer isRead;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
