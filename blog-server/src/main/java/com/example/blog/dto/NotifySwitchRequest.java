package com.example.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 邮件通知开关（v2.1 用户级） */
@Data
public class NotifySwitchRequest {

    @NotNull(message = "开关状态不能为空")
    private Boolean enabled;
}
