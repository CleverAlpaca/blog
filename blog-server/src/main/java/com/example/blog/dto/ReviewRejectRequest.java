package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员退回投稿请求 */
@Data
public class ReviewRejectRequest {

    @NotBlank(message = "退回原因不能为空")
    @Size(max = 255, message = "退回原因最长 255 字")
    private String reason;
}
