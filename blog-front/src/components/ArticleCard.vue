<script setup lang="ts">
import { computed } from 'vue'
import type { ArticleCard } from '@/types/api'
import { formatCount, formatDateTime } from '@/utils/format'
import AppIcon from './AppIcon.vue'

const props = defineProps<{ article: ArticleCard }>()

/** 无封面时用渐变占位背景（按文章 id 平滑轮换） */
const coverFallback = computed(() => {
  const palettes = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  ]
  const index = Math.abs(Number(props.article.id) || 0) % palettes.length
  return palettes[index]
})

const coverUrl = computed(() => {
  const url = props.article.coverImage
  if (!url) return ''
  // 相对路径 /upload/** 直接走同源（dev 由 proxy 转发）
  if (/^https?:\/\//i.test(url) || url.startsWith('/')) return url
  return `/${url}`
})
</script>

<template>
  <RouterLink :to="`/article/${article.id}`" class="article-card card card-hover">
    <div class="cover">
      <img v-if="coverUrl" :src="coverUrl" :alt="article.title" loading="lazy" @error="($event.target as HTMLImageElement).style.display = 'none'" />
      <div v-else class="cover-fallback" :style="{ background: coverFallback }">
        <span class="fallback-letter">{{ article.title.charAt(0) || '文' }}</span>
      </div>
      <span v-if="article.isTop === 1" class="top-badge">
        <AppIcon name="bookmark" :size="12" />
        置顶
      </span>
    </div>

    <div class="body">
      <h3 class="title">{{ article.title }}</h3>
      <p class="summary">{{ article.summary || '（暂无摘要）' }}</p>

      <div class="meta">
        <div class="meta-left">
          <RouterLink v-if="article.categoryName" :to="`/category/${article.categoryId}`" class="category" @click.stop>
            <AppIcon name="folder" :size="13" />
            {{ article.categoryName }}
          </RouterLink>
          <div v-if="article.tags && article.tags.length" class="tags">
            <RouterLink v-for="tag in article.tags.slice(0, 3)" :key="tag.id" :to="`/tag/${tag.id}`" class="chip chip-plain" @click.stop>
              {{ tag.name }}
            </RouterLink>
          </div>
        </div>
        <div class="stats">
          <span class="stat" :title="`发表于 ${formatDateTime(article.createTime)}`">
            <AppIcon name="calendar" :size="13" />
            {{ formatDateTime(article.createTime).slice(0, 10) }}
          </span>
          <span class="stat" title="浏览量">
            <AppIcon name="eye" :size="13" />
            {{ formatCount(article.views) }}
          </span>
          <span class="stat" title="点赞数">
            <AppIcon name="heart" :size="13" />
            {{ formatCount(article.likes) }}
          </span>
          <span class="stat" title="评论数">
            <AppIcon name="message-circle" :size="13" />
            {{ formatCount(article.commentCount) }}
          </span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.article-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  height: 100%;
}

.cover {
  position: relative;
  width: 220px;
  height: 140px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: var(--color-cover-soft);
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .cover img {
  transform: scale(1.05);
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fallback-letter {
  font-size: 44px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
}

.top-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(239, 68, 68, 0.92);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
}

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s ease;
}

.article-card:hover .title {
  color: var(--color-primary);
}

.summary {
  flex: 1;
  margin-top: 6px;
  font-size: 13.5px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-top: 10px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.category {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-primary);
  font-weight: 500;
}

.category:hover {
  text-decoration: underline;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--color-text-tertiary);
  font-size: 12.5px;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

@media (max-width: 560px) {
  .article-card {
    flex-direction: column;
  }

  .cover {
    width: 100%;
    height: 160px;
  }
}
</style>
