package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.blog.common.BusinessException;
import com.example.blog.common.ErrorCode;
import com.example.blog.dto.CategorySaveRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.service.CategoryService;
import com.example.blog.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> list() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort).orderByAsc(Category::getId));
        if (categories.isEmpty()) {
            return List.of();
        }
        // 已发布文章按分类计数（数据量小，分组统计即可）
        List<Map<String, Object>> rows = articleMapper.selectMaps(new QueryWrapper<Article>()
                .select("category_id", "COUNT(*) AS cnt")
                .eq("status", 1)
                .isNotNull("category_id")
                .groupBy("category_id"));
        Map<Long, Long> counts = rows.stream().collect(Collectors.toMap(
                m -> ((Number) m.get("category_id")).longValue(),
                m -> ((Number) m.get("cnt")).longValue()));
        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setDescription(c.getDescription());
            vo.setSort(c.getSort());
            vo.setArticleCount(counts.getOrDefault(c.getId(), 0L));
            return vo;
        }).toList();
    }

    @Override
    public void save(CategorySaveRequest request) {
        checkNameUnique(request.getName(), null);
        Category c = new Category();
        c.setName(request.getName());
        c.setDescription(orDefault(request.getDescription()));
        c.setSort(request.getSort() == null ? 0 : request.getSort());
        categoryMapper.insert(c);
    }

    @Override
    public void update(Long id, CategorySaveRequest request) {
        requireExists(id);
        checkNameUnique(request.getName(), id);
        Category c = new Category();
        c.setId(id);
        c.setName(request.getName());
        c.setDescription(orDefault(request.getDescription()));
        c.setSort(request.getSort() == null ? 0 : request.getSort());
        categoryMapper.updateById(c);
    }

    @Override
    public void delete(Long id) {
        requireExists(id);
        Long articleCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (articleCount > 0) {
            throw new BusinessException(400, "该分类下仍有文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private void requireExists(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<Category>().eq(Category::getName, name);
        if (excludeId != null) {
            w.ne(Category::getId, excludeId);
        }
        if (categoryMapper.selectCount(w) > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }
    }

    private String orDefault(String s) {
        return s == null ? "" : s;
    }
}
