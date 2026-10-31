package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 个人主页（用户公开资料，不含邮箱等敏感字段） */
@Data
public class UserHomeVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String intro;

    /** 注册日期（yyyy-MM-dd） */
    private String regDate;

    /** 身份标识：admin=博主 user=社区作者 */
    private String badge;
}
