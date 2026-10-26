package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.FriendLink;
import com.example.blog.entity.Message;
import com.example.blog.entity.Tag;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.FriendLinkMapper;
import com.example.blog.mapper.MessageMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.CommentService;
import com.example.blog.service.DashboardService;
import com.example.blog.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final MessageMapper messageMapper;
    private final FriendLinkMapper friendLinkMapper;
    private final CommentService commentService;

    @Override
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setPublishedCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)));
        vo.setDraftCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 0)));
        vo.setCategoryCount(categoryMapper.selectCount(null));
        vo.setTagCount(tagMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1)));
        vo.setMessageCount(messageMapper.selectCount(
                new LambdaQueryWrapper<Message>().eq(Message::getStatus, 1)));
        vo.setFriendLinkCount(friendLinkMapper.selectCount(
                new LambdaQueryWrapper<FriendLink>().eq(FriendLink::getStatus, 1)));

        List<Map<String, Object>> sums = articleMapper.selectMaps(new QueryWrapper<Article>()
                .select("IFNULL(SUM(views),0) AS totalViews", "IFNULL(SUM(likes),0) AS totalLikes"));
        Map<String, Object> row = sums.isEmpty() ? Map.of() : sums.get(0);
        vo.setTotalViews(row.isEmpty() ? 0 : ((Number) row.get("totalViews")).longValue());
        vo.setTotalLikes(row.isEmpty() ? 0 : ((Number) row.get("totalLikes")).longValue());

        vo.setLatestComments(commentService.latest(5));
        return vo;
    }
}
