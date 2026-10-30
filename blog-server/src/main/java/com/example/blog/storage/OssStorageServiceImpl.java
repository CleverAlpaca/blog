package com.example.blog.storage;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * S3 兼容图床实现（blog.storage.type=oss 时启用）。
 * 走 AWS S3 SDK + path-style：MinIO / 阿里云 OSS（S3 兼容端点）/ AWS 均可接入。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "blog.storage.type", havingValue = "oss")
public class OssStorageServiceImpl implements FileStorageService {

    private final AmazonS3 s3;
    private final String bucket;
    private final String baseUrl;

    public OssStorageServiceImpl(StorageProperties properties) {
        StorageProperties.Oss oss = properties.getOss();
        requireText(oss.getEndpoint(), "endpoint");
        requireText(oss.getAccessKeyId(), "access-key-id");
        requireText(oss.getAccessKeySecret(), "access-key-secret");
        requireText(oss.getBucket(), "bucket");

        ClientConfiguration config = new ClientConfiguration();
        config.setSignerOverride("AWSS3V4SignerType");
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(oss.getEndpoint(), "us-east-1"))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(oss.getAccessKeyId(), oss.getAccessKeySecret())))
                .withClientConfiguration(config)
                .enablePathStyleAccess()
                .build();
        this.bucket = oss.getBucket();
        this.baseUrl = isBlank(oss.getHost())
                ? trimTrailingSlash(oss.getEndpoint()) + "/" + oss.getBucket()
                : trimTrailingSlash(oss.getHost());
    }

    @Override
    public String upload(MultipartFile file) {
        String ext = ImageValidator.extOf(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ImageValidator.checkMagicBytes(in.readNBytes(12), ext);
        } catch (IOException e) {
            throw new BusinessException(400, "读取上传文件失败");
        }

        String key = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType("image/" + (ext.equals("jpg") ? "jpeg" : ext));
            s3.putObject(bucket, key, in, metadata);
        } catch (IOException e) {
            log.error("S3 上传失败", e);
            throw new BusinessException(ErrorCode.SERVER_ERROR, "上传文件失败");
        }
        return baseUrl + "/" + key;
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "blog.storage.type=oss 需要完整配置 blog.storage.oss 的 " + name + " 等参数");
        }
    }

    private String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
