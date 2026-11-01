package com.example.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path base = Paths.get(uploadDir).toAbsolutePath();
            // 启动即创建目录：Path.toUri() 仅对已存在的目录补尾部斜杠，
            // 缺斜杠会让资源定位拼出 "dir2026/11/x.png" 导致本地图床 404
            Files.createDirectories(base);
            String location = base.toUri().toString();
            if (!location.endsWith("/")) {
                location += "/";
            }
            registry.addResourceHandler("/upload/**")
                    .addResourceLocations(location);
        } catch (Exception e) {
            throw new IllegalStateException("上传目录初始化失败: " + uploadDir, e);
        }
    }

    /**
     * 开发环境跨域放行；生产环境由 Nginx 同域转发，此配置不生效也无碍
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
