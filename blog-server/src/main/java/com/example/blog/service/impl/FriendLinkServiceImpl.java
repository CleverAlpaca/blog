package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.FriendLinkSaveRequest;
import com.example.blog.entity.FriendLink;
import com.example.blog.mapper.FriendLinkMapper;
import com.example.blog.service.FriendLinkService;
import com.example.blog.vo.FriendLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendLinkServiceImpl implements FriendLinkService {

    private final FriendLinkMapper friendLinkMapper;

    @Override
    public List<FriendLinkVO> listVisible() {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                        .eq(FriendLink::getStatus, 1)
                        .orderByAsc(FriendLink::getSort).orderByAsc(FriendLink::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<FriendLinkVO> listAdmin() {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                        .orderByAsc(FriendLink::getSort).orderByAsc(FriendLink::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public void save(FriendLinkSaveRequest request) {
        FriendLink link = new FriendLink();
        apply(link, request);
        friendLinkMapper.insert(link);
    }

    @Override
    public void update(Long id, FriendLinkSaveRequest request) {
        if (friendLinkMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "友链不存在");
        }
        FriendLink link = new FriendLink();
        link.setId(id);
        apply(link, request);
        friendLinkMapper.updateById(link);
    }

    @Override
    public void delete(Long id) {
        if (friendLinkMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "友链不存在");
        }
        friendLinkMapper.deleteById(id);
    }

    private void apply(FriendLink link, FriendLinkSaveRequest request) {
        link.setName(request.getName());
        link.setLogo(orDefault(request.getLogo()));
        link.setUrl(request.getUrl());
        link.setDescription(orDefault(request.getDescription()));
        link.setSort(request.getSort() == null ? 0 : request.getSort());
        // 透传不设默认：新增时 null 走数据库默认值 1；更新时 null 保留原状态（不把隐藏的友链改回显示）
        link.setStatus(request.getStatus());
    }

    private FriendLinkVO toVO(FriendLink link) {
        FriendLinkVO vo = new FriendLinkVO();
        vo.setId(link.getId());
        vo.setName(link.getName());
        vo.setLogo(link.getLogo());
        vo.setUrl(link.getUrl());
        vo.setDescription(link.getDescription());
        vo.setSort(link.getSort());
        vo.setStatus(link.getStatus());
        return vo;
    }

    private String orDefault(String s) {
        return s == null ? "" : s;
    }
}
