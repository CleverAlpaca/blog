package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleSaveRequest;
import com.example.blog.vo.ArchiveYearVO;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.ArticleDetailVO;

import java.util.List;

public interface ArticleService {

    /** 前台：文章分页（仅已发布，置顶优先，支持分类/标签/关键词筛选） */
    PageResult<ArticleCardVO> pageFront(long pageNum, long pageSize, Long categoryId, Long tagId, String keyword);

    /** 前台：文章详情（浏览量进 Redis 缓冲，含上下篇与当前访客点赞状态） */
    ArticleDetailVO detail(Long id, String visitorId);

    /** 后台：编辑回显（任意状态，不做浏览量/导航/点赞填充） */
    ArticleDetailVO getForEdit(Long id);

    /** 前台：热门文章 Top 5 */
    List<ArticleCardVO> hot();

    /** 前台：按年份分组的归档 */
    List<ArchiveYearVO> archives();

    /** 后台：文章分页 */
    PageResult<ArticleCardVO> pageAdmin(long pageNum, long pageSize, String keyword, Integer status, Long categoryId);

    Long save(ArticleSaveRequest request);

    void update(Long id, ArticleSaveRequest request);

    /** 逻辑删除文章并隐藏其评论 */
    void delete(Long id);

    void setTop(Long id, Integer isTop);
}
