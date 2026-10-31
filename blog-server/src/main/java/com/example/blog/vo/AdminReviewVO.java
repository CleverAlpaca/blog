package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 后台投稿审核列表项 */
@Data
public class AdminReviewVO {

    private Long id;

    private String title;

    private Long authorId;

    private String authorName;

    /** 1 已审核通过 / 2 已下架（曾通过审核）/ 3 待审核 / 4 已退回 */
    private Integer status;

    private String rejectReason;

    private LocalDateTime submitTime;

    /** 审核通过时间（仅通过审核的投稿有值） */
    private LocalDateTime reviewTime;
}
