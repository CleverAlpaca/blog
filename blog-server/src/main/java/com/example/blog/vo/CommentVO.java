package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 两级评论树：顶级评论 + 楼内回复（文章评论与留言板共用结构） */
@Data
public class CommentVO {

    private Long id;

    /** 评论者用户ID（历史游客评论为 null；可据此链接到个人主页） */
    private Long userId;

    private Long parentId;

    private String nickname;

    private String content;

    private LocalDateTime createTime;

    private List<CommentVO> replies;
}
