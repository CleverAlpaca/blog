package com.example.blog.storage;

import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import java.util.UUID;

/**
 * 本地磁盘图床实现（默认：blog.storage.type=local 或未配置）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "blog.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements FileStorageService {

    private final Path baseDir;

    public LocalStorageServiceImpl(@Value("${blog.upload-dir}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String upload(MultipartFile file) {
        String ext = ImageValidator.extOf(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ImageValidator.checkMagicBytes(in.readNBytes(12), ext);
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
            throw new BusinessException(ErrorCode.SERVER_ERROR, "保存上传文件失败");
        }
        return "/upload/" + relativeDir + "/" + filename;
    }
}
