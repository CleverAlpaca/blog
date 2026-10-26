package com.example.blog.storage;

import com.example.blog.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path baseDir;

    public LocalStorageServiceImpl(@Value("${blog.upload-dir}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile file) {
        String ext = extOf(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg / png / gif / webp 格式图片");
        }
        try (InputStream in = file.getInputStream()) {
            checkMagicBytes(in.readNBytes(12), ext);
        } catch (IOException e) {
            throw new BusinessException(400, "读取上传文件失败");
        }

        String relativeDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = baseDir.resolve(relativeDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            log.error("保存上传文件失败", e);
            throw new BusinessException(500, "保存上传文件失败");
        }
        return "/upload/" + relativeDir + "/" + filename;
    }

    private String extOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(400, "文件缺少后缀名");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 校验文件头与后缀一致，防止伪造后缀上传脚本
     */
    private void checkMagicBytes(byte[] head, String ext) {
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
