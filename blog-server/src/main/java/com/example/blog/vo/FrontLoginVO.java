package com.example.blog.vo;

import lombok.Data;

/** 前台登录/注册结果 */
@Data
public class FrontLoginVO {

    private String token;

    /** ADMIN / USER */
    private String role;

    private UserVO user;
}
