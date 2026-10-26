package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 文章列表卡片（前台首页 / 后台文章管理列表共用） */
@Data
public class ArticleCardVO {

    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    /** 0 草稿 / 1 已发布 / 2 已下架（后台列表用，前台忽略） */
    private Integer status;

    private Integer isTop;

    private Long categoryId;

    private String categoryName;

    private List<TagVO> tags;

    private Long views;

    private Long likes;

    private Integer commentCount;

    private LocalDateTime createTime;
}
