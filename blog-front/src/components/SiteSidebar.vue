<script setup lang="ts">
import type { ArticleCard } from '@/types/api'
import { getHotArticles } from '@/api/article'
import { useRequest } from '@/composables/useRequest'
import { useSiteStore } from '@/stores/site'
import { formatCount } from '@/utils/format'
import AppIcon from './AppIcon.vue'
import LoadState from './LoadState.vue'

const siteStore = useSiteStore()
const { data: hotArticles, loading, error, run } = useRequest<ArticleCard[]>(getHotArticles)

function coverLetter(title: string): string {
  return title.charAt(0) || '文'
}
</script>

<template>
  <aside class="side-column">
    <!-- 站点信息卡 -->
    <div class="card site-card">
      <div class="author">
        <img
          v-if="siteStore.about?.avatar"
          :src="siteStore.about.avatar"
          :alt="siteStore.about.nickname"
          class="avatar"
          @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
        />
        <div v-else class="avatar avatar-fallback">
          <AppIcon name="user" :size="26" />
        </div>
        <h3 class="nickname">{{ siteStore.about?.nickname || "Alpaca's Blog" }}</h3>
        <p class="intro">{{ siteStore.about?.intro || '记录技术，分享生活。' }}</p>
      </div>

      <div class="stat-grid">
        <RouterLink to="/archive" class="stat-cell">
          <strong>{{ siteStore.stats?.articleCount ?? '--' }}</strong>
          <span>
            <AppIcon name="file-text" :size="12" />
            文章
          </span>
        </RouterLink>
        <RouterLink to="/categories" class="stat-cell">
          <strong>{{ siteStore.stats?.categoryCount ?? '--' }}</strong>
          <span>
            <AppIcon name="folder" :size="12" />
            分类
          </span>
        </RouterLink>
        <RouterLink to="/tags" class="stat-cell">
          <strong>{{ siteStore.stats?.tagCount ?? '--' }}</strong>
          <span>
            <AppIcon name="tag" :size="12" />
            标签
          </span>
        </RouterLink>
        <div class="stat-cell">
          <strong>{{ siteStore.stats?.runDays ?? '--' }}</strong>
          <span>
            <AppIcon name="clock" :size="12" />
            运行天数
          </span>
        </div>
      </div>
    </div>

    <!-- 站点公告（后台「站点配置」维护，留空隐藏） -->
    <div v-if="siteStore.announcement" class="card announcement-card">
      <h3 class="card-title">公告</h3>
      <p class="announcement-text">{{ siteStore.announcement }}</p>
    </div>

    <!-- 热门文章 Top5 -->
    <div class="card hot-card">
      <h3 class="card-title">热门文章</h3>
      <LoadState :loading="loading" :error="error" :empty="!hotArticles?.length" empty-text="暂无热门文章" empty-icon="trending-up" @retry="run">
        <ol class="hot-list">
          <li v-for="(article, index) in hotArticles ?? []" :key="article.id" class="hot-item">
            <span class="rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
            <RouterLink :to="`/article/${article.id}`" class="hot-title">{{ article.title }}</RouterLink>
            <span class="hot-views">
              <AppIcon name="eye" :size="12" />
              {{ formatCount(article.views) }}
            </span>
          </li>
        </ol>
      </LoadState>
    </div>
  </aside>
</template>

<style scoped>
.side-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: calc(var(--header-height) + 16px);
}

@media (max-width: 768px) {
  .side-column {
    position: static;
  }
}

/* 站点信息卡 */
.site-card {
  padding: 24px 20px;
  text-align: center;
  background: var(--color-card);
}

.announcement-card {
  margin-top: 16px;
}

.announcement-text {
  font-size: 13.5px;
  line-height: 1.8;
  color: var(--color-text-secondary);
  white-space: pre-line;
  word-break: break-word;
}

.author {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--color-card);
  box-shadow: var(--shadow-card);
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.nickname {
  margin-top: 4px;
  font-size: 17px;
  font-weight: 700;
}

.intro {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.stat-grid {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed var(--color-border);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  border-radius: var(--radius-small);
  padding: 8px 4px;
  transition: background 0.2s ease;
}

.stat-cell:hover {
  background: var(--color-primary-soft);
}

.stat-cell strong {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
}

.stat-cell span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

/* 热门文章 */
.hot-card {
  padding: 18px 16px 10px;
}

.hot-list {
  counter-reset: hot;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 2px;
  border-bottom: 1px solid var(--color-border);
}

.hot-item:last-child {
  border-bottom: none;
}

.rank {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: var(--color-chip);
  color: var(--color-text-tertiary);
  font-size: 12px;
  font-weight: 700;
}

.rank-1 {
  background: var(--danger-soft);
  color: var(--color-danger);
}

.rank-2 {
  background: var(--rank-2-bg);
  color: var(--rank-2-text);
}

.rank-3 {
  background: var(--rank-3-bg);
  color: var(--rank-3-text);
}

.hot-title {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.hot-title:hover {
  color: var(--color-primary);
}

.hot-views {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
