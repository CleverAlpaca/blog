package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 用户投稿/存草稿请求 */
@Data
public class MyArticleSaveRequest {

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

    /** draft 存草稿 / submit 提交审核 */
    @NotBlank(message = "action 不能为空")
    @Pattern(regexp = "draft|submit", message = "action 仅支持 draft 或 submit")
    private String action;
}
