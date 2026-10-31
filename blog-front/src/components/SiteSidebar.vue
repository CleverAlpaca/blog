<script setup lang="ts">
import { computed } from 'vue'
import type { ArticleCard } from '@/types/api'
import { getHotArticles } from '@/api/article'
import { useRequest } from '@/composables/useRequest'
import { useSiteStore } from '@/stores/site'
import { useUserStore } from '@/stores/user'
import { formatCount } from '@/utils/format'
import AppIcon from './AppIcon.vue'
import LoadState from './LoadState.vue'

const siteStore = useSiteStore()
const userStore = useUserStore()
const { data: hotArticles, loading, error, run } = useRequest<ArticleCard[]>(getHotArticles)

// 身份区随登录态切换：登录显示当前用户，访客显示博主（站点信息）
const displayName = computed(() =>
  userStore.isLoggedIn && userStore.user?.nickname
    ? userStore.user.nickname
    : siteStore.about?.nickname || "Alpaca's Blog"
)
const displayIntro = computed(() =>
  userStore.isLoggedIn
    ? userStore.user?.intro || '还没有填写简介'
    : siteStore.about?.intro || '记录技术，分享生活。'
)
const displayAvatar = computed(() =>
  userStore.isLoggedIn ? userStore.user?.avatar : siteStore.about?.avatar
)
const displayBadge = computed(() =>
  userStore.isLoggedIn ? (userStore.isAdmin ? '博主' : '作者') : '博主'
)

function coverLetter(title: string): string {
  return title.charAt(0) || '文'
}
</script>

<template>
  <aside class="side-column">
    <!-- 站点信息卡：登录后展示当前用户，访客展示博主 -->
    <div class="card site-card">
      <div class="author">
        <img
          v-if="displayAvatar"
          :src="displayAvatar"
          :alt="displayName"
          class="avatar"
          @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
        />
        <div v-else class="avatar avatar-fallback">
          <AppIcon name="user" :size="26" />
        </div>
        <h3 class="nickname">
          {{ displayName }}
          <span class="role-badge" :class="{ admin: userStore.isAdmin && userStore.isLoggedIn }">{{ displayBadge }}</span>
        </h3>
        <p class="intro">{{ displayIntro }}</p>
        <RouterLink v-if="userStore.isLoggedIn && userStore.user" :to="`/user/${userStore.user.id}`" class="me-link">
          进入我的主页 →
        </RouterLink>
      </div>

      <div class="stat-grid">
        <p class="stat-caption">全站统计</p>
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.role-badge {
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;
  background: var(--color-chip);
  color: var(--color-text-secondary);
}

.role-badge.admin {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.me-link {
  font-size: 12.5px;
  color: var(--color-primary);
  font-weight: 600;
}

.me-link:hover {
  text-decoration: underline;
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

.stat-caption {
  grid-column: 1 / -1;
  margin: -4px 0 0;
  text-align: center;
  font-size: 11px;
  color: var(--color-text-tertiary);
  letter-spacing: 0.08em;
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
