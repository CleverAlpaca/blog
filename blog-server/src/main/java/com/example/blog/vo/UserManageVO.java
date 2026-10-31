package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 后台用户管理列表项 */
@Data
public class UserManageVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String role;

    private Integer status;

    private LocalDateTime createTime;

    /** 已发布文章数 */
    private Long articleCount;
}
