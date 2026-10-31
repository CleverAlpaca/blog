package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.entity.Article;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserManageService;
import com.example.blog.vo.UserManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserManageVO> pageUsers(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword), q -> q.like(User::getUsername, keyword).or().like(User::getNickname, keyword))
                .eq(status != null, User::getStatus, status)
                .orderByAsc(User::getId);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), w);

        List<Long> ids = page.getRecords().stream().map(User::getId).toList();
        Map<Long, Long> counts = ids.isEmpty() ? Map.of()
                : articleMapper.selectMaps(new QueryWrapper<Article>()
                        .select("user_id", "COUNT(*) AS cnt")
                        .in("user_id", ids)
                        .groupBy("user_id"))
                .stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("user_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()));

        List<UserManageVO> vos = page.getRecords().stream().map(u -> {
            UserManageVO vo = new UserManageVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setEmail(u.getEmail());
            vo.setRole(u.getRole());
            vo.setStatus(u.getStatus());
            vo.setCreateTime(u.getCreateTime());
            vo.setArticleCount(counts.getOrDefault(u.getId(), 0L));
            return vo;
        }).toList();
        return new PageResult<>(vos, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值不合法");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(400, "不能封禁管理员账号");
        }
        if (user.getStatus().equals(status)) {
            return;
        }
        userMapper.update(null, new UpdateWrapper<User>().eq("id", id).set("status", status));
        if (status == 0) {
            // 封禁即踢下线
            StpUtil.logout(id);
        }
    }

    @Override
    public String resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException(400, "管理员密码请在「个人资料」中自行修改");
        }
        String newPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", id)
                .set("password", passwordEncoder.encode(newPassword)));
        StpUtil.logout(id);
        return newPassword;
    }
}
