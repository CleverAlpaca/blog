package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.entity.SiteConfig;
import com.example.blog.mapper.SiteConfigMapper;
import com.example.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    /** 对外公开的配置键白名单（内部键不会出现在前台接口） */
    public static final Set<String> PUBLIC_KEYS = Set.of("site_name", "site_description", "announcement", "icp");

    private final SiteConfigMapper siteConfigMapper;

    @Override
    public Map<String, String> getAllMap() {
        return siteConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(SiteConfig::getConfigKey, SiteConfig::getConfigValue, (a, b) -> a));
    }

    @Override
    public Map<String, String> getPublicMap() {
        Map<String, String> all = getAllMap();
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : PUBLIC_KEYS) {
            result.put(key, all.getOrDefault(key, ""));
        }
        return result;
    }

    @Override
    public String getValue(String key, String fallback) {
        SiteConfig config = siteConfigMapper.selectOne(
                new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, key));
        return config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()
                ? fallback : config.getConfigValue();
    }

    @Override
    @Transactional
    public void updateBatch(Map<String, String> configs) {
        Map<String, String> existing = getAllMap();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            if (key == null || key.isBlank() || key.length() > 50) {
                throw new BusinessException(400, "配置键不合法（1~50 字）");
            }
            if (value.length() > 1000) {
                throw new BusinessException(400, "配置值最长 1000 字");
            }
            if (existing.containsKey(key)) {
                siteConfigMapper.update(null, new UpdateWrapper<SiteConfig>()
                        .eq("config_key", key)
                        .set("config_value", value));
            } else {
                SiteConfig config = new SiteConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                siteConfigMapper.insert(config);
            }
        }
    }
}
