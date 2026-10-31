package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录用户发表评论请求（决策点 2：仅登录可评论，
 * 昵称/邮箱由账号派生，不再由请求提供）
 */
@Data
public class CommentPostRequest {

    /** 父评论ID（顶级评论不传；楼内回复传顶级评论ID） */
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容最长 1000 字")
    private String content;
}
