package com.example.blog.storage;

import com.example.blog.common.BusinessException;

import java.util.Locale;
import java.util.Set;

/**
 * 图片文件校验工具：后缀白名单 + 文件头魔数校验，本地/OSS 存储实现共用
 */
public final class ImageValidator {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private ImageValidator() {
    }

    public static String extOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(400, "文件缺少后缀名");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg / png / gif / webp 格式图片");
        }
        return ext;
    }

    /**
     * 校验文件头与后缀一致，防止伪造后缀上传脚本
     */
    public static void checkMagicBytes(byte[] head, String ext) {
        boolean ok = switch (ext) {
            case "jpg", "jpeg" -> head.length >= 3
                    && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
            case "png" -> head.length >= 4
                    && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
            case "gif" -> head.length >= 4 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8';
            case "webp" -> head.length >= 12
                    && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            default -> false;
        };
        if (!ok) {
            throw new BusinessException(400, "文件内容与后缀名不符");
        }
    }
}
