package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 登录用户发表留言请求（昵称由账号派生） */
@Data
public class MessagePostRequest {

    /** 父留言ID（顶级留言不传） */
    private Long parentId;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容最长 1000 字")
    private String content;
}
