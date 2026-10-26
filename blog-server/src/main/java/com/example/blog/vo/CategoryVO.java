package com.example.blog.vo;

import lombok.Data;

@Data
public class CategoryVO {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    /** 已发布文章数（前台/看板展示用） */
    private Long articleCount;
}
