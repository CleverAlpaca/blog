package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import com.example.blog.mail.MailService;
import com.example.blog.service.EmailCodeService;
import com.example.blog.util.RateLimitUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final int MAX_FAILS = 5;
    private static final Set<String> SCENES = Set.of("register", "reset", "bind");
    private static final Map<String, String> SUBJECTS = Map.of(
            "register", "注册验证码",
            "reset", "找回密码验证码",
            "bind", "换绑邮箱验证码");

    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;
    private final RateLimitUtil rateLimitUtil;
    private final UserMapper userMapper;
    private final SecureRandom random = new SecureRandom();

    @Override
    public void sendCode(String scene, String email, String ip) {
        if (!SCENES.contains(scene)) {
            throw new BusinessException(400, "不支持的验证码场景");
        }
        String mail = email == null ? "" : email.trim();
        if (!StringUtils.hasText(mail)) {
            throw new BusinessException(400, "邮箱不能为空");
        }
        // 双层限频：同邮箱 60s 1 条 + 同 IP 每小时 10 条
        rateLimitUtil.check("email-code:box", mail, 1, Duration.ofMinutes(1));
        rateLimitUtil.check("email-code:ip", ip, 10, Duration.ofHours(1));

        if ("register".equals(scene)) {
            // 注册场景可直接提示占用（注册页本就公开校验邮箱可用性）
            if (countByEmail(mail, null) > 0) {
                throw new BusinessException(400, "该邮箱已被注册");
            }
        } else if ("reset".equals(scene)) {
            // 防枚举：账号不存在或未绑定邮箱时静默成功，不发信
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, mail));
            if (user == null) {
                return;
            }
        } else {
            // 换绑需登录
            StpUtil.checkLogin();
            if (countByEmail(mail, StpUtil.getLoginIdAsLong()) > 0) {
                throw new BusinessException(400, "该邮箱已被其他账号绑定");
            }
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        stringRedisTemplate.opsForValue().set(key(scene, mail), code, CODE_TTL);
        stringRedisTemplate.delete(failKey(scene, mail));
        mailService.send(mail, "【个人博客】" + SUBJECTS.get(scene),
                "你的验证码是 " + code + "，5 分钟内有效。若非本人操作，请忽略本邮件。");
    }

    @Override
    public void verifyCode(String scene, String email, String code) {
        String mail = email == null ? "" : email.trim();
        String k = key(scene, mail);
        String saved = stringRedisTemplate.opsForValue().get(k);
        if (saved == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!saved.equals(code)) {
            String fk = failKey(scene, mail);
            Long fails = stringRedisTemplate.opsForValue().increment(fk);
            if (fails != null && fails == 1) {
                stringRedisTemplate.expire(fk, CODE_TTL);
            }
            if (fails != null && fails >= MAX_FAILS) {
                stringRedisTemplate.delete(k);
                stringRedisTemplate.delete(fk);
                throw new BusinessException(400, "错误次数过多，验证码已作废，请重新获取");
            }
            throw new BusinessException(400, "验证码错误");
        }
        // 一次性消费
        stringRedisTemplate.delete(k);
    }

    private long countByEmail(String email, Long excludeUserId) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        if (excludeUserId != null) {
            w.ne(User::getId, excludeUserId);
        }
        return userMapper.selectCount(w);
    }

    private String key(String scene, String email) {
        return "blog:email-code:" + scene + ":" + email;
    }

    private String failKey(String scene, String email) {
        return "blog:email-code-fail:" + scene + ":" + email;
    }
}
