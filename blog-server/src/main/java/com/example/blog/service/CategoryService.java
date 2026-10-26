package com.example.blog.service;

import com.example.blog.dto.CategorySaveRequest;
import com.example.blog.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /** 前台/后台通用：分类列表（含已发布文章数），按 sort 升序 */
    List<CategoryVO> list();

    void save(CategorySaveRequest request);

    void update(Long id, CategorySaveRequest request);

    void delete(Long id);
}
