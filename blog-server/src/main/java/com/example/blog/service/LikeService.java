package com.example.blog.service;

import com.example.blog.vo.LikeVO;

public interface LikeService {

    /**
     * 切换点赞状态：未赞则点赞，已赞则取消（幂等开关）
     *
     * @param articleId 文章ID
     * @param visitorId 访客标识（X-Visitor-Id 请求头；为空时由调用方回退为 IP 标识）
     */
    LikeVO toggle(Long articleId, String visitorId);
}
