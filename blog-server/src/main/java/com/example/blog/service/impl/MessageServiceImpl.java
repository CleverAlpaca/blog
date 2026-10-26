package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.common.PageResult;
import com.example.blog.dto.MessagePostRequest;
import com.example.blog.entity.Message;
import com.example.blog.mapper.MessageMapper;
import com.example.blog.service.MessageService;
import com.example.blog.util.RateLimitUtil;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final int RATE_LIMIT_PER_MINUTE = 5;

    private final MessageMapper messageMapper;
    private final RateLimitUtil rateLimitUtil;

    @Override
    public List<MessageVO> tree() {
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getStatus, 1)
                .orderByAsc(Message::getCreateTime).orderByAsc(Message::getId));
        Map<Long, MessageVO> voMap = new LinkedHashMap<>();
        List<MessageVO> tops = new ArrayList<>();
        for (Message m : messages) {
            MessageVO vo = toVO(m);
            vo.setReplies(new ArrayList<>());
            voMap.put(m.getId(), vo);
            MessageVO parent = m.getParentId() == null ? null : voMap.get(m.getParentId());
            if (parent == null) {
                tops.add(vo);
            } else {
                parent.getReplies().add(vo);
            }
        }
        return tops;
    }

    @Override
    public void post(MessagePostRequest request, String ip) {
        rateLimitUtil.check("message", ip, RATE_LIMIT_PER_MINUTE);
        Message message = new Message();
        message.setParentId(normalizeParentId(request.getParentId()));
        message.setNickname(request.getNickname().trim());
        message.setEmail(orDefault(request.getEmail()));
        message.setContent(request.getContent().trim());
        message.setStatus(1);
        message.setIp(ip);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }

    private Long normalizeParentId(Long parentId) {
        if (parentId == null) {
            return null;
        }
        Message parent = messageMapper.selectById(parentId);
        if (parent == null || parent.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "回复的留言不存在或已隐藏");
        }
        return parent.getParentId() != null ? parent.getParentId() : parent.getId();
    }

    @Override
    public PageResult<AdminCommentVO> pageAdmin(long pageNum, long pageSize, String nickname) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .like(StringUtils.hasText(nickname), Message::getNickname, nickname)
                .orderByDesc(Message::getCreateTime).orderByDesc(Message::getId);
        Page<Message> page = messageMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<AdminCommentVO> vos = page.getRecords().stream().map(this::toAdminVO).toList();
        return new PageResult<>(vos, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值不合法");
        }
        if (messageMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "留言不存在");
        }
        messageMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Message>()
                .eq("id", id).set("status", status));
    }

    @Override
    public void delete(Long id) {
        if (messageMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "留言不存在");
        }
        messageMapper.deleteById(id);
    }

    private AdminCommentVO toAdminVO(Message m) {
        AdminCommentVO vo = new AdminCommentVO();
        vo.setId(m.getId());
        vo.setNickname(m.getNickname());
        vo.setContent(m.getContent());
        vo.setStatus(m.getStatus());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    private MessageVO toVO(Message m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setParentId(m.getParentId());
        vo.setNickname(m.getNickname());
        vo.setContent(m.getContent());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    private String orDefault(String s) {
        return s == null ? "" : s;
    }
}
