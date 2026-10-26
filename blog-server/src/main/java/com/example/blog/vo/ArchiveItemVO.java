package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ArchiveItemVO {

    private Long id;

    private String title;

    private LocalDateTime createTime;
}
