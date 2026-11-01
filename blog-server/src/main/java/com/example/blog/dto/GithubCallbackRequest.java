package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** GitHub OAuth 回调换取登录态（前台落地页提交） */
@Data
public class GithubCallbackRequest {

    @NotBlank(message = "code 不能为空")
    private String code;

    @NotBlank(message = "state 不能为空")
    private String state;
}
