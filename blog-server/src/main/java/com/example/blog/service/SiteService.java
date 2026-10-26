package com.example.blog.service;

import com.example.blog.vo.AboutVO;
import com.example.blog.vo.SiteStatsVO;

public interface SiteService {

    /** 关于页：博主信息 + 「关于我」文章内容 */
    AboutVO about();

    /** 站点统计 */
    SiteStatsVO stats();
}
