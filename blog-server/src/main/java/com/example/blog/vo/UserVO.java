package com.example.blog.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String intro;

    private String email;
}
