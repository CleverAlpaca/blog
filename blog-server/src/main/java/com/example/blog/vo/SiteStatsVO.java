package com.example.blog.vo;

import lombok.Data;

@Data
public class SiteStatsVO {

    /** 已发布文章数 */
    private Long articleCount;

    private Long categoryCount;

    private Long tagCount;

    private Long commentCount;

    /** 站点运行天数（以最早发布文章起算） */
    private Long runDays;
}
