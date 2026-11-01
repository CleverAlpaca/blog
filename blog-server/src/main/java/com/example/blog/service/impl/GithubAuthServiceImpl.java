package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.dto.GithubCallbackRequest;
import com.example.blog.entity.User;
import com.example.blog.mapper.UserMapper;
import com.example.blog.oauth.GithubProperties;
import com.example.blog.service.GithubAuthService;
import com.example.blog.util.RateLimitUtil;
import com.example.blog.vo.FrontLoginVO;
import com.example.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GitHub OAuth 登录（评审 §4.1）：authorize 生成一次性 state；
 * callback 校验 state → code 换 token → 拉用户/邮箱 → 绑定或自动注册 → 登录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubAuthServiceImpl implements GithubAuthService {

    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_API = "https://api.github.com/user";
    private static final String EMAIL_API = "https://api.github.com/user/emails";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final GithubProperties properties;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitUtil rateLimitUtil;
    private final RestClient restClient = RestClient.create();

    @Override
    public String authorizeUrl(String ip) {
        // 同 IP 授权跳转限频，防刷
        rateLimitUtil.check("github:authorize", ip, 20, Duration.ofHours(1));
        requireConfigured();
        String state = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(stateKey(state), "1", STATE_TTL);
        return AUTHORIZE_URL + "?client_id=" + properties.getClientId()
                + "&redirect_uri=" + properties.getRedirectUri()
                + "&scope=read:user%20user:email"
                + "&state=" + state;
    }

    @Override
    public FrontLoginVO login(GithubCallbackRequest request) {
        String stateKey = stateKey(request.getState());
        Boolean held = stringRedisTemplate.hasKey(stateKey);
        if (!Boolean.TRUE.equals(held)) {
            throw new BusinessException(400, "授权状态已过期，请重新发起 GitHub 登录");
        }
        stringRedisTemplate.delete(stateKey); // 一次性，防重放

        String accessToken = exchangeToken(request.getCode());
        Map<?, ?> ghUser = restClient.get()
                .uri(USER_API)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(Map.class);
        if (ghUser == null || ghUser.get("id") == null) {
            throw new BusinessException(400, "GitHub 用户信息获取失败，请重试");
        }
        long githubId = ((Number) ghUser.get("id")).longValue();
        Object ghLoginRaw = ghUser.get("login");
        String ghLogin = ghLoginRaw == null ? "github" + githubId : String.valueOf(ghLoginRaw);
        String avatar = ghUser.get("avatar_url") == null ? "" : String.valueOf(ghUser.get("avatar_url"));
        String intro = ghUser.get("bio") == null ? "" : String.valueOf(ghUser.get("bio"));
        String email = pickPrimaryEmail(accessToken);

        // 0) 已登录用户发起「绑定」：GitHub 账号绑到当前账号（设置页入口，请求携带会话 token）
        Object currentUidObj = StpUtil.getLoginIdDefaultNull();
        Long currentUid = currentUidObj == null ? null : Long.valueOf(currentUidObj.toString());
        if (currentUid != null) {
            User current = userMapper.selectById(currentUid);
            if (current == null) {
                throw new BusinessException(400, "登录状态异常，请重新登录");
            }
            User bound = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGithubId, githubId));
            if (bound != null && !bound.getId().equals(currentUid)) {
                throw new BusinessException(400, "该 GitHub 账号已绑定其他站内账号");
            }
            if (bound == null) {
                userMapper.update(null, new UpdateWrapper<User>()
                        .eq("id", currentUid)
                        .set("github_id", githubId));
                current.setGithubId(githubId);
            }
            StpUtil.login(current.getId());
            return buildLoginVO(current);
        }

        // 1) 已绑定该 GitHub 账号 → 直接登录
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGithubId, githubId));
        if (user == null) {
            // 2) GitHub 邮箱命中已注册账号 → 绑定（该账号未绑定其他 GitHub 时）
            if (email != null) {
                user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
                if (user != null && user.getGithubId() != null && !user.getGithubId().equals(githubId)) {
                    throw new BusinessException(400, "该邮箱已绑定其他 GitHub 账号");
                }
            }
            // 3) 自动注册（D4=B）：GitHub 昵称/邮箱/头像，邮箱不可得时用 noreply 占位（D5=B）
            if (user == null) {
                user = registerFromGithub(githubId, ghLogin, email, avatar, intro);
            } else {
                userMapper.update(null, new UpdateWrapper<User>()
                        .eq("id", user.getId())
                        .set("github_id", githubId));
                user.setGithubId(githubId);
            }
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }
        StpUtil.login(user.getId());
        return buildLoginVO(user);
    }

    /** 绑定/注册补全后的统一登录态 */
    private FrontLoginVO buildLoginVO(User user) {
        FrontLoginVO vo = new FrontLoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setRole(user.getRole());
        UserVO u = new UserVO();
        u.setId(user.getId());
        u.setUsername(user.getUsername());
        u.setNickname(user.getNickname());
        u.setAvatar(user.getAvatar());
        u.setIntro(user.getIntro());
        u.setEmail(user.getEmail());
        u.setRole(user.getRole());
        u.setGithubId(user.getGithubId());
        u.setEmailNotify(user.getEmailNotify());
        vo.setUser(u);
        return vo;
    }

    private User registerFromGithub(long githubId, String ghLogin, String email, String avatar, String intro) {
        User user = new User();
        user.setUsername(availableUsername(ghLogin, githubId));
        // 密码随机不可用：该账号走 GitHub 登录，后续可经邮箱验证码找回/换绑
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setNickname(ghLogin);
        user.setEmail(email == null ? githubId + "+" + ghLogin + "@users.noreply.github.com" : email);
        user.setAvatar(avatar);
        user.setIntro(intro);
        user.setGithubId(githubId);
        user.setRole("USER");
        user.setStatus(1);
        user.setEmailNotify(1);
        userMapper.insert(user);
        return user;
    }

    /** GitHub login 字符集含连字符，归一为用户名规则并处理撞名 */
    private String availableUsername(String ghLogin, long githubId) {
        String base = ghLogin.replaceAll("-", "_");
        if (!base.matches("^[a-zA-Z0-9_]{4,32}$")) {
            base = "gh_" + base;
        }
        if (base.length() > 32) {
            base = base.substring(0, 32);
        }
        String candidate = base;
        int i = 1;
        while (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, candidate)) > 0) {
            candidate = (base + i).length() > 32 ? base.substring(0, 32 - String.valueOf(i).length()) + i : base + i;
            i++;
        }
        return candidate;
    }

    /** 取 primary+verified 邮箱；没有则 null（由 D5 占位策略兜底） */
    @SuppressWarnings("unchecked")
    private String pickPrimaryEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri(EMAIL_API)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .body(List.class);
            if (emails == null) {
                return null;
            }
            String fallback = null;
            for (Map<String, Object> item : emails) {
                boolean verified = Boolean.TRUE.equals(item.get("verified"));
                if (!verified) {
                    continue;
                }
                if (Boolean.TRUE.equals(item.get("primary"))) {
                    return String.valueOf(item.get("email"));
                }
                if (fallback == null) {
                    fallback = String.valueOf(item.get("email"));
                }
            }
            return fallback;
        } catch (Exception e) {
            log.warn("GitHub 邮箱列表获取失败，退回 noreply 占位", e);
            return null;
        }
    }

    private String exchangeToken(String code) {
        Map<?, ?> resp;
        try {
            resp = restClient.post()
                    .uri(TOKEN_URL)
                    .header("Accept", "application/json")
                    .body(Map.of(
                            "client_id", properties.getClientId(),
                            "client_secret", properties.getClientSecret(),
                            "code", code,
                            "redirect_uri", properties.getRedirectUri()))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("GitHub access_token 换取失败", e);
            throw new BusinessException(400, "GitHub 登录失败，请重试");
        }
        Object token = resp == null ? null : resp.get("access_token");
        if (token == null || String.valueOf(token).isBlank()) {
            throw new BusinessException(400, "GitHub 授权码无效或已过期，请重新登录");
        }
        return String.valueOf(token);
    }

    private void requireConfigured() {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            throw new BusinessException(400, "GitHub 登录未配置（缺少 client-id/secret），请先在部署配置中填写");
        }
    }

    private String stateKey(String state) {
        return "blog:github:state:" + state;
    }
}
