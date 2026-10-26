package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TagVO {

    private Long id;

    private String name;

    /** 关联的已发布文章数（标签云用，文章卡片内为 null） */
    private Long count;

    public TagVO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagVO(Long id, String name, Long count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }
}
