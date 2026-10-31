package com.example.blog.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 角色数据源：/api/admin/** 的 ADMIN 角色校验依赖此实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userMapper.selectById(Long.valueOf(String.valueOf(loginId)));
        return user == null ? List.of() : List.of(user.getRole());
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 权限点暂未启用：角色即权限
        return List.of();
    }
}
