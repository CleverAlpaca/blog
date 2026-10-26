package com.example.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessagePostRequest {

    /** 父留言ID（顶级留言不传） */
    private Long parentId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称最长 32 字")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱过长")
    private String email;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容最长 1000 字")
    private String content;
}
