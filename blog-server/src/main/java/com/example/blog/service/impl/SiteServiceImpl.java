package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.SiteConfigService;
import com.example.blog.service.SiteService;
import com.example.blog.vo.AboutVO;
import com.example.blog.vo.SiteStatsVO;
import com.example.blog.vo.UserHomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final SiteConfigService siteConfigService;

    @Value("${blog.site-name}")
    private String siteName;

    @Value("${blog.site-url}")
    private String siteUrl;

    @Override
    public AboutVO about() {
        User user = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getStatus, 1)
                        .orderByAsc(User::getId)
                        .last("LIMIT 1"))
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "站点未初始化"));
        Article aboutArticle = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(Article::getTitle, "关于我")
                .orderByDesc(Article::getId)
                .last("LIMIT 1"));
        AboutVO vo = new AboutVO();
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIntro(user.getIntro());
        vo.setEmail(user.getEmail());
        vo.setContent(aboutArticle == null ? null : aboutArticle.getContent());
        return vo;
    }

    @Override
    public SiteStatsVO stats() {
        SiteStatsVO vo = new SiteStatsVO();
        vo.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)));
        vo.setCategoryCount(categoryMapper.selectCount(null));
        vo.setTagCount(tagMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1)));
        // 运行天数：以最早发布的文章起算
        Article earliest = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByAsc(Article::getCreateTime)
                .last("LIMIT 1"));
        long runDays = 0;
        if (earliest != null && earliest.getCreateTime() != null) {
            runDays = Math.max(1, ChronoUnit.DAYS.between(
                    earliest.getCreateTime().toLocalDate(), LocalDate.now()) + 1);
        }
        vo.setRunDays(runDays);
        return vo;
    }

    @Override
    public String rss() {
        List<Article> list = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime).orderByDesc(Article::getId)
                .select(Article::getId, Article::getTitle, Article::getSummary, Article::getCreateTime)
                .last("LIMIT 20"));

        // 站点名称/描述优先取站点配置表，未配置时回退 application.yml 属性
        String name = siteConfigService.getValue("site_name", siteName);
        String description = siteConfigService.getValue("site_description", name + " 最新文章");
        DateTimeFormatter fmt = DateTimeFormatter.RFC_1123_DATE_TIME
                .withLocale(Locale.ENGLISH).withZone(ZoneId.of("GMT"));
        StringBuilder xml = new StringBuilder(1024);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\">\n<channel>\n");
        xml.append("<title>").append(escape(name)).append("</title>\n");
        xml.append("<link>").append(escape(siteUrl)).append("</link>\n");
        xml.append("<description>").append(escape(description)).append("</description>\n");
        for (Article a : list) {
            String link = siteUrl + "/article/" + a.getId();
            xml.append("<item>\n")
                    .append("<title>").append(escape(a.getTitle())).append("</title>\n")
                    .append("<link>").append(escape(link)).append("</link>\n")
                    .append("<guid isPermaLink=\"true\">").append(escape(link)).append("</guid>\n")
                    .append("<pubDate>").append(fmt.format(a.getCreateTime().atZone(ZoneId.of("Asia/Shanghai")))).append("</pubDate>\n")
                    .append("<description>").append(escape(a.getSummary())).append("</description>\n")
                    .append("</item>\n");
        }
        xml.append("</channel>\n</rss>");
        return xml.toString();
    }

    @Override
    public UserHomeVO userHome(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在或已被封禁");
        }
        UserHomeVO vo = new UserHomeVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setIntro(user.getIntro());
        vo.setRegDate(user.getCreateTime() == null ? ""
                : user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        vo.setBadge("ADMIN".equals(user.getRole()) ? "admin" : "user");
        return vo;
    }

    private String escape(String s) {
        return s == null ? "" : s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    public String sitemap() {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime)
                .select(Article::getId, Article::getUpdateTime));

        DateTimeFormatter day = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String base = trimTrailingSlash(siteUrl);
        StringBuilder xml = new StringBuilder(1024);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String path : new String[] { "/", "/archive", "/categories", "/tags", "/message", "/friends", "/about" }) {
            xml.append("<url><loc>").append(escape(base + path)).append("</loc></url>\n");
        }
        for (Article a : articles) {
            xml.append("<url><loc>").append(escape(base + "/article/" + a.getId())).append("</loc>");
            if (a.getUpdateTime() != null) {
                xml.append("<lastmod>").append(day.format(a.getUpdateTime())).append("</lastmod>");
            }
            xml.append("</url>\n");
        }
        xml.append("</urlset>");
        return xml.toString();
    }

    @Override
    public String robots() {
        String base = trimTrailingSlash(siteUrl);
        return "User-agent: *\n"
                + "Allow: /\n"
                + "Disallow: /admin\n"
                + "Disallow: /api\n"
                + "\n"
                + "Sitemap: " + base + "/api/front/sitemap.xml\n";
    }

    private String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
