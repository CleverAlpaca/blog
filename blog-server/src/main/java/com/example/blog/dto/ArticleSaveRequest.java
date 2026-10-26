package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增与修改文章共用（修改时 id 由路径参数传递）
 */
@Data
public class ArticleSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    @Size(max = 500, message = "摘要最长 500 字")
    private String summary;

    private String coverImage;

    private Long categoryId;

    private List<Long> tagIds;

    @NotBlank(message = "正文不能为空")
    @Size(max = 50000, message = "正文最长 5 万字")
    private String content;

    /** 0 草稿 / 1 已发布 / 2 已下架，默认草稿 */
    private Integer status;

    private Integer isTop;
}
