package com.example.blog.service;

import com.example.blog.dto.FriendLinkSaveRequest;
import com.example.blog.vo.FriendLinkVO;

import java.util.List;

public interface FriendLinkService {

    /** 前台：仅显示中的友链，按 sort 升序 */
    List<FriendLinkVO> listVisible();

    /** 后台：全部友链 */
    List<FriendLinkVO> listAdmin();

    void save(FriendLinkSaveRequest request);

    void update(Long id, FriendLinkSaveRequest request);

    void delete(Long id);
}
