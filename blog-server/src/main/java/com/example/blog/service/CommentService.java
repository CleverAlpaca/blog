package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.CommentPostRequest;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.CommentVO;
import com.example.blog.vo.UserCommentVO;

import java.util.List;

public interface CommentService {

    /** 前台：某文章的两级评论树（仅显示中的） */
    List<CommentVO> tree(Long articleId);

    /** 前台：发表评论（含限频与楼内回复归一化） */
    void post(Long articleId, CommentPostRequest request, String ip);

    /** 前台个人主页：某用户的公开评论分页（仅显示中且文章已发布） */
    PageResult<UserCommentVO> pageByUser(Long userId, long pageNum, long pageSize);

    /** 后台：评论分页（articleTitle/nickname 均为模糊筛选，可空） */
    PageResult<AdminCommentVO> pageAdmin(long pageNum, long pageSize, String articleTitle, String nickname);

    /** 后台：显示/隐藏（联动文章冗余评论数） */
    void updateStatus(Long id, Integer status);

    void delete(Long id);

    /** 后台看板：最新 N 条评论 */
    List<AdminCommentVO> latest(int limit);
}
