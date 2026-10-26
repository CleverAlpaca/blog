package com.example.blog.service;

import com.example.blog.dto.TagSaveRequest;
import com.example.blog.vo.TagVO;

import java.util.List;

public interface TagService {

    /** 标签列表（含已发布文章数），前台标签云与后台通用 */
    List<TagVO> list();

    void save(TagSaveRequest request);

    void update(Long id, TagSaveRequest request);

    void delete(Long id);
}
