package com.example.blog.vo;

import lombok.Data;

@Data
public class AboutVO {

    private String nickname;

    private String avatar;

    private String intro;

    private String email;

    /** 「关于我」文章正文（Markdown） */
    private String content;
}
