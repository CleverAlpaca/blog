package com.example.blog.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 图片存储配置：blog.storage.type = local（默认）| oss
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.storage")
public class StorageProperties {

    /** 存储类型：local 本地磁盘 / oss 阿里云 OSS（或兼容 S3 协议的自建服务） */
    private String type = "local";

    private Oss oss = new Oss();

    @Data
    public static class Oss {

        /** Endpoint，如 oss-cn-hangzhou.aliyuncs.com */
        private String endpoint;

        private String accessKeyId;

        private String accessKeySecret;

        private String bucket;

        /** 图片访问域名（可选：CDN/自定义域名），不填则用 https://{bucket}.{endpoint} */
        private String host;
    }
}
