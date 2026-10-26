package com.example.blog.service;

import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.PasswordUpdateRequest;
import com.example.blog.dto.ProfileUpdateRequest;
import com.example.blog.vo.LoginVO;
import com.example.blog.vo.UserVO;

public interface UserService {

    LoginVO login(LoginRequest request);

    UserVO profile(Long userId);

    void updateProfile(Long userId, ProfileUpdateRequest request);

    void updatePassword(Long userId, PasswordUpdateRequest request);
}
