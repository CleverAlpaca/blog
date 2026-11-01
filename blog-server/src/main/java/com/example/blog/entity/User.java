package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String intro;

    /** ADMIN 管理员 / USER 普通用户（多用户扩展预留） */
    private String role;

    private Integer status;

    /** GitHub 账号 ID（OAuth 绑定，NULL 未绑定） */
    private Long githubId;

    /** 邮件通知开关：1 开 / 0 关（审核结果通知） */
    private Integer emailNotify;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
