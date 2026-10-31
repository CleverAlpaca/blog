package com.example.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailCodeSendRequest {

    /** 场景：register 注册 / reset 找回密码 / bind 换绑邮箱 */
    @NotBlank(message = "场景不能为空")
    private String scene;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @jakarta.validation.constraints.Size(max = 128, message = "邮箱过长")
    private String email;
}
