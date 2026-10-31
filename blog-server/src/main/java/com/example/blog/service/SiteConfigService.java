package com.example.blog.service;

import java.util.Map;

public interface SiteConfigService {

    /** 全部配置（键值对），后台管理用 */
    Map<String, String> getAllMap();

    /** 对外公开的配置子集（site_name / site_description / announcement / icp），前台用 */
    Map<String, String> getPublicMap();

    /** 读取单个配置值，未配置或为空时返回 fallback */
    String getValue(String key, String fallback);

    /** 批量保存（存在则更新，不存在则插入） */
    void updateBatch(Map<String, String> configs);
}
