package com.example.blog.vo;

import lombok.Data;

@Data
public class FriendLinkVO {

    private Long id;

    private String name;

    private String logo;

    private String url;

    private String description;

    private Integer sort;

    /** 1 显示 / 0 隐藏（后台管理用） */
    private Integer status;
}
