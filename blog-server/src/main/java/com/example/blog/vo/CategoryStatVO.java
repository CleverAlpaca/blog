package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 看板图表：单个分类的文章数 */
@Data
@AllArgsConstructor
public class CategoryStatVO {

    private String name;

    private Long count;
}
