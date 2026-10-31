package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AuthService;
import com.example.blog.service.UserService;
import com.example.blog.util.RateLimitUtil;
import com.example.blog.vo.FrontLoginVO;
import com.example.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitUtil rateLimitUtil;

    @Override
    public FrontLoginVO register(RegisterRequest request, String ip) {
        rateLimitUtil.check("register", ip, 5, Duration.ofHours(1));
        String username = request.getUsername().trim();
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new BusinessException(400, "用户名已被占用");
        }
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        if (!email.isEmpty()
                && userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new BusinessException(400, "邮箱已被占用");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null || request.getNickname().isBlank()
                ? username : request.getNickname().trim());
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        StpUtil.login(user.getId());
        return buildLoginVO(user);
    }

    @Override
    public FrontLoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁，请联系管理员");
        }
        StpUtil.login(user.getId());
        return buildLoginVO(user);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO profile() {
        return userService.profile(StpUtil.getLoginIdAsLong());
    }

    @Override
    public void updateProfile(ProfileUpdateRequest request) {
        userService.updateProfile(StpUtil.getLoginIdAsLong(), request);
    }

    @Override
    public void updatePassword(PasswordUpdateRequest request) {
        userService.updatePassword(StpUtil.getLoginIdAsLong(), request);
    }

    private FrontLoginVO buildLoginVO(User user) {
        FrontLoginVO vo = new FrontLoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setRole(user.getRole());
        vo.setUser(toVO(user));
        return vo;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIntro(user.getIntro());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        return vo;
    }
}
