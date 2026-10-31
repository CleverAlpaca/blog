package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 状态：0 草稿 / 1 已发布 / 2 已下架 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String summary;

    private String coverImage;

    private String content;

    private Integer status;

    private Integer isTop;

    private Long views;

    private Long likes;

    private Integer commentCount;

    /** 退回原因（status=4 时展示给作者） */
    private String rejectReason;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
