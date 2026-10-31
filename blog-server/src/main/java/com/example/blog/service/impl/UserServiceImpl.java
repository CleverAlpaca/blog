package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserService;
import com.example.blog.vo.LoginVO;
import com.example.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        StpUtil.login(user.getId());
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUser(toVO(user));
        return vo;
    }

    @Override
    public UserVO profile(Long userId) {
        return toVO(requireExists(userId));
    }

    @Override
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        requireExists(userId);
        User user = new User();
        user.setId(userId);
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setIntro(request.getIntro());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = requireExists(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码不正确");
        }
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", userId)
                .set("password", passwordEncoder.encode(request.getNewPassword())));
        // 密码变更后让所有已登录会话失效，需重新登录
        StpUtil.logout(userId);
    }

    private User requireExists(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIntro(user.getIntro());
        vo.setEmail(user.getEmail());
        return vo;
    }
}
