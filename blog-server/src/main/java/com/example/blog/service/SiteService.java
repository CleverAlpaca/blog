package com.example.blog.service;

import com.example.blog.vo.AboutVO;
import com.example.blog.vo.SiteStatsVO;

public interface SiteService {

    /** 关于页：博主信息 + 「关于我」文章内容 */
    AboutVO about();

    /** 站点统计 */
    SiteStatsVO stats();

    /** RSS 2.0 订阅源（最新 20 篇已发布文章） */
    String rss();

    /** sitemap.xml（SEO 站点地图：固定页 + 全部已发布文章） */
    String sitemap();

    /** robots.txt（允许全部、屏蔽 /admin 与 /api、声明 Sitemap 地址） */
    String robots();
}
