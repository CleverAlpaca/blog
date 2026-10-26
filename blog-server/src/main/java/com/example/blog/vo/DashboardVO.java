package com.example.blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {

    private Long publishedCount;

    private Long draftCount;

    private Long categoryCount;

    private Long tagCount;

    private Long commentCount;

    private Long messageCount;

    private Long friendLinkCount;

    private Long totalViews;

    private Long totalLikes;

    private List<AdminCommentVO> latestComments;
}
