package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 留言板：结构与 comment 一致，仅少 article_id */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long parentId;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String ip;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}
