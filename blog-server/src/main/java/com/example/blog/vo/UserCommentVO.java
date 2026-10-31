package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 个人主页：用户公开评论项 */
@Data
public class UserCommentVO {

    private Long id;

    /** 所属文章 */
    private Long articleId;

    private String articleTitle;

    /** 回复的评论 ID（顶级评论为 null） */
    private Long parentId;

    private String content;

    private LocalDateTime createTime;
}
