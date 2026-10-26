package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.MessagePostRequest;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /** 前台：两级留言树（仅显示中的） */
    List<MessageVO> tree();

    void post(MessagePostRequest request, String ip);

    /** 后台：留言分页（nickname 模糊筛选，可空） */
    PageResult<AdminCommentVO> pageAdmin(long pageNum, long pageSize, String nickname);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
