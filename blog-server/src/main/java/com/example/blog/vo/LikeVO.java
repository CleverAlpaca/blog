package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeVO {

    /** 点赞后为 true，取消后为 false */
    private boolean liked;

    private Long likes;
}
