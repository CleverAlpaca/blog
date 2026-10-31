<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserComments, getUserHome, getUserArticles } from '@/api/userPage'
import { usePageTitle } from '@/composables/usePageTitle'
import type { ArticleCard, PageResult, UserCommentItem, UserHome } from '@/types/api'
import { formatDateTime } from '@/utils/format'
import ArticleCardVue from '@/components/ArticleCard.vue'
import AppPagination from '@/components/AppPagination.vue'
import AppIcon from '@/components/AppIcon.vue'
import LoadState from '@/components/LoadState.vue'

const route = useRoute()
const userId = computed(() => Number(route.params.id))

const profile = ref<UserHome | null>(null)
const page = ref<PageResult<ArticleCard> | null>(null)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const error = ref(false)

/** 内容区 Tab：文章 / 评论 */
const activeTab = ref<'articles' | 'comments'>('articles')
const cpage = ref<PageResult<UserCommentItem> | null>(null)
const cPageNum = ref(1)
const cLoading = ref(false)
const cError = ref(false)

usePageTitle(() => (profile.value ? `${profile.value.nickname} 的主页` : '个人主页'))

async function load(): Promise<void> {
  if (!Number.isInteger(userId.value)) {
    error.value = true
    return
  }
  loading.value = true
  try {
    page.value = await getUserArticles(userId.value, { pageNum: pageNum.value, pageSize })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function loadComments(): Promise<void> {
  if (!Number.isInteger(userId.value)) {
    cError.value = true
    return
  }
  cLoading.value = true
  try {
    cpage.value = await getUserComments(userId.value, { pageNum: cPageNum.value, pageSize })
  } catch {
    cError.value = true
  } finally {
    cLoading.value = false
  }
}

function switchTab(tab: 'articles' | 'comments'): void {
  if (activeTab.value === tab) return
  activeTab.value = tab
  // 评论列表首次切入时加载（后续翻页在分页组件回调里处理）
  if (tab === 'comments' && !cpage.value) void loadComments()
}

function onPageChange(page: number): void {
  pageNum.value = page
  void load()
}

function onCommentPageChange(page: number): void {
  cPageNum.value = page
  void loadComments()
}

onMounted(async () => {
  loading.value = true
  try {
    profile.value = await getUserHome(userId.value)
    await load()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
  // 顺带加载评论首页：徽标直接显示真实总数，切 Tab 免等待
  void loadComments()
})
</script>

<template>
  <div class="user-home-page">
    <LoadState :loading="loading && !profile" :error="error && !profile" @retry="load">
      <div v-if="profile" class="card profile-card">
        <img
          v-if="profile.avatar"
          :src="profile.avatar"
          :alt="profile.nickname"
          class="avatar"
          @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
        />
        <div v-else class="avatar avatar-fallback">
          <AppIcon name="user" :size="30" />
        </div>
        <div class="profile-info">
          <h1 class="nickname">
            {{ profile.nickname }}
            <span class="badge" :class="profile.badge">{{ profile.badge === 'admin' ? '博主' : '作者' }}</span>
          </h1>
          <p class="intro">{{ profile.intro || '这位作者还没有写简介' }}</p>
          <p class="reg">加入于 {{ profile.regDate }}</p>
        </div>
      </div>

      <div class="tabs" role="tablist">
        <button type="button" class="tab" :class="{ active: activeTab === 'articles' }" @click="switchTab('articles')">
          <AppIcon name="file-text" :size="15" />
          文章
          <span class="tab-count">{{ page?.total ?? 0 }}</span>
        </button>
        <button type="button" class="tab" :class="{ active: activeTab === 'comments' }" @click="switchTab('comments')">
          <AppIcon name="message-circle" :size="15" />
          评论
          <span class="tab-count">{{ cpage?.total ?? 0 }}</span>
        </button>
      </div>

      <!-- 文章 Tab -->
      <LoadState
        v-if="activeTab === 'articles'"
        :loading="loading"
        :error="error"
        :empty="(page?.records?.length ?? 0) === 0"
        empty-text="还没有已发布的文章"
      >
        <div class="article-list">
          <ArticleCardVue v-for="a in page?.records ?? []" :key="a.id" :article="a" />
        </div>
        <AppPagination
          v-if="(page?.total ?? 0) > pageSize"
          :page-num="pageNum"
          :page-size="pageSize"
          :total="page?.total ?? 0"
          @change="onPageChange"
        />
      </LoadState>

      <!-- 评论 Tab -->
      <LoadState
        v-else
        :loading="cLoading"
        :error="cError"
        :empty="(cpage?.records?.length ?? 0) === 0"
        empty-text="还没有发表过公开评论"
        empty-icon="message-circle"
        @retry="loadComments"
      >
        <div class="comment-list">
          <div v-for="c in cpage?.records ?? []" :key="c.id" class="comment-item card">
            <p class="comment-content">{{ c.content }}</p>
            <div class="comment-meta">
              <RouterLink :to="`/article/${c.articleId}`" class="comment-article">
                <AppIcon name="file-text" :size="13" />
                {{ c.articleTitle || '原文章' }}
                <span v-if="c.parentId" class="reply-mark">楼内回复</span>
              </RouterLink>
              <span class="comment-time">{{ formatDateTime(c.createTime) }}</span>
            </div>
          </div>
        </div>
        <AppPagination
          v-if="(cpage?.total ?? 0) > pageSize"
          :page-num="cPageNum"
          :page-size="pageSize"
          :total="cpage?.total ?? 0"
          @change="onCommentPageChange"
        />
      </LoadState>
    </LoadState>
  </div>
</template>

<style scoped>
.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 20px;
}

.avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--color-card);
  box-shadow: var(--shadow-card);
  flex-shrink: 0;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.nickname {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge.admin {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.badge.user {
  background: var(--color-chip);
  color: var(--color-text-secondary);
}

.intro {
  margin-top: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.reg {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 18px;
}

/* 内容区 Tab */
.tabs {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--color-border);
}

.tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 14px;
  margin-bottom: -1px;
  border: none;
  border-bottom: 2px solid transparent;
  background: none;
  font-size: 14.5px;
  font-weight: 600;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}

.tab:hover {
  color: var(--color-text);
}

.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.tab-count {
  min-width: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--color-chip);
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.tab.active .tab-count {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 18px;
}

.comment-item {
  padding: 14px 16px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.65;
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
  font-size: 12.5px;
}

.comment-article {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  color: var(--color-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.comment-article:hover {
  color: var(--color-primary);
}

.reply-mark {
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--color-chip);
  color: var(--color-text-secondary);
  font-size: 11.5px;
}

.comment-time {
  flex-shrink: 0;
  color: var(--color-text-tertiary);
}
</style>
