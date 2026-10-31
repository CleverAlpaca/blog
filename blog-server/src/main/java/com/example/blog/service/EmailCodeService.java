package com.example.blog.service;

/**
 * 邮箱验证码：生成、限频发送、校验消费（Redis 5 分钟 TTL + 一次性 + 错 5 次作废）
 */
public interface EmailCodeService {

    /** 场景：register 注册 / reset 找回密码 / bind 换绑邮箱 */
    void sendCode(String scene, String email, String ip);

    /** 校验并消费验证码（不匹配/过期/超限抛业务异常） */
    void verifyCode(String scene, String email, String code);
}
