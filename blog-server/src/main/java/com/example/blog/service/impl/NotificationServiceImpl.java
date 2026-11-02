package com.example.blog.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.PageResult;
import com.example.blog.entity.Notification;
import com.example.blog.mapper.NotificationMapper;
import com.example.blog.service.NotificationService;
import com.example.blog.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int CONTENT_MAX = 80;

    private final NotificationMapper notificationMapper;

    @Override
    public void push(Long userId, String type, String title, String content, String link) {
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setType(type);
            n.setTitle(title);
            n.setContent(truncate(content));
            n.setLink(link == null ? "" : link);
            n.setIsRead(0);
            notificationMapper.insert(n);
        } catch (Exception e) {
            // 通知是旁路能力：失败仅记日志，不影响评论/留言主流程
            log.error("站内通知写入失败 userId={} type={}", userId, type, e);
        }
    }

    @Override
    public PageResult<NotificationVO> pageMine(long pageNum, long pageSize) {
        long uid = StpUtil.getLoginIdAsLong();
        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, uid)
                        .orderByDesc(Notification::getCreateTime).orderByDesc(Notification::getId));
        List<NotificationVO> vos = page.getRecords().stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setLink(n.getLink());
            vo.setIsRead(n.getIsRead());
            vo.setCreateTime(n.getCreateTime());
            return vo;
        }).toList();
        return new PageResult<>(vos, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public long unreadCount() {
        long uid = StpUtil.getLoginIdAsLong();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, uid)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id) {
        long uid = StpUtil.getLoginIdAsLong();
        // 归属校验：只能标记自己的通知
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, uid)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllRead() {
        long uid = StpUtil.getLoginIdAsLong();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, uid)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > CONTENT_MAX ? content.substring(0, CONTENT_MAX) + "…" : content;
    }
}
