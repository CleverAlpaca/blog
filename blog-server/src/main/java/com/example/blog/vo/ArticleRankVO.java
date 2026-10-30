package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 看板图表：单篇文章浏览量 */
@Data
@AllArgsConstructor
public class ArticleRankVO {

    private Long id;

    private String title;

    private Long views;
}
