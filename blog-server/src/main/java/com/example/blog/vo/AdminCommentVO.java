package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 后台评论/留言管理列表项（留言时 articleId、articleTitle 为 null） */
@Data
public class AdminCommentVO {

    private Long id;

    private Long articleId;

    private String articleTitle;

    private String nickname;

    private String content;

    private Integer status;

    private LocalDateTime createTime;
}
