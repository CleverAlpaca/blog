package com.example.blog.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String intro;

    private String email;

    /** ADMIN / USER（前台按此区分用户态） */
    private String role;

    /** GitHub 绑定状态（非空=已绑定） */
    private Long githubId;

    /** 邮件通知开关：1 开 / 0 关 */
    private Integer emailNotify;
}
