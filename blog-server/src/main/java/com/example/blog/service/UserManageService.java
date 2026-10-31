package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.vo.UserManageVO;

public interface UserManageService {

    /** 用户分页（keyword 匹配用户名/昵称；status 过滤；含已发布文章数） */
    PageResult<UserManageVO> pageUsers(long pageNum, long pageSize, String keyword, Integer status);

    /** 封禁 / 解封（ADMIN 不可封禁；封禁即踢下线） */
    void updateStatus(Long id, Integer status);

    /** 重置密码（生成 8 位随机密码返回一次，并踢下线） */
    String resetPassword(Long id);
}
