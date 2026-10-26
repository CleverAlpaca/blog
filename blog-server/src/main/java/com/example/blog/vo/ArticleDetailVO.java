package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO {

    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    private Long categoryId;

    private String categoryName;

    private List<TagVO> tags;

    /** Markdown 原文 */
    private String content;

    private Integer status;

    private Integer isTop;

    private Long views;

    private Long likes;

    private Integer commentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long prevId;

    private String prevTitle;

    private Long nextId;

    private String nextTitle;

    /** 当前访客是否已点赞（后台编辑回显时为 null） */
    private Boolean liked;
}
