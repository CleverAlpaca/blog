package com.example.blog.service;

import com.example.blog.common.PageResult;
import com.example.blog.dto.ArticleSaveRequest;
import com.example.blog.dto.MyArticleSaveRequest;
import com.example.blog.vo.AdminReviewVO;
import com.example.blog.vo.ArchiveYearVO;
import com.example.blog.vo.ArticleCardVO;
import com.example.blog.vo.ArticleDetailVO;

import java.util.Map;
import java.util.List;

public interface ArticleService {

    /** 前台：文章分页（仅已发布，置顶优先，支持分类/标签/关键词/作者筛选） */
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

    /** 后台：投稿审核分页（status: 3 待审核 / 4 已退回，可空=全部） */
    PageResult<AdminReviewVO> pageReview(long pageNum, long pageSize, Integer status, String keyword);

    /** 审核通过：待审核 → 已发布 */
    void approve(Long id);

    /** 退回：待审核 → 已退回（原因必填，作者可见） */
    void reject(Long id, String reason);

    /** 我的投稿分页（当前登录用户全部状态，按更新时间倒序） */
    PageResult<ArticleCardVO> pageMine(long pageNum, long pageSize, Integer status);

    /** 我的投稿各状态数量：draft / pending / returned / published */
    Map<String, Long> statsMine();

    /** 投稿 / 存草稿（作者=当前登录用户；action=draft|submit） */
    Long createMine(MyArticleSaveRequest request);

    /** 编辑自己的草稿或被退回稿（action 决定存草稿还是重新提交审核） */
    void updateMine(Long id, MyArticleSaveRequest request);

    /** 删除自己的非已发布文章 */
    void deleteMine(Long id);

    /** 我的投稿编辑回显（仅本人，含退回原因） */
    ArticleDetailVO getMineForEdit(Long id);

    /** 某用户的已发布文章分页（个人主页用） */
    PageResult<ArticleCardVO> pagePublishedByUser(Long userId, long pageNum, long pageSize);
}
