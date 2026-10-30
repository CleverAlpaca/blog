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

    /** 浏览量 Top 5 文章（已发布） */
    private List<ArticleRankVO> topArticles;

    /** 各分类已发布文章数（仅含文章数大于 0 的分类） */
    private List<CategoryStatVO> categoryDistribution;
}
