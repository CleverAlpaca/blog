package com.example.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片存储接口：本期实现本地磁盘，后续可无缝切换 OSS/MinIO
 */
public interface FileStorageService {

    /**
     * 保存图片并返回可访问的相对路径（/upload/yyyy/MM/uuid.ext）
     */
    String upload(MultipartFile file);
}
