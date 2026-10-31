package com.example.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageVO {

    private Long id;

    /** 留言者用户ID（历史游客留言为 null） */
    private Long userId;

    private Long parentId;

    private String nickname;

    private String content;

    private LocalDateTime createTime;

    private List<MessageVO> replies;
}
