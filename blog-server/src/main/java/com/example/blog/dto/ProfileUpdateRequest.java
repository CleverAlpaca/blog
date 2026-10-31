package com.example.blog.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String nickname;

    private String avatar;

    /** v2.0 起邮箱变更走 PUT /api/front/auth/email（验证码校验），此处不再接受 */
    private String intro;
}
