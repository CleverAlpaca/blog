<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import type { ArticleDetail } from '@/types/api'
import { getArticleDetail, toggleLike } from '@/api/article'
import { getCommentTree, postComment } from '@/api/comment'
import type { CommentPost } from '@/types/api'
import { usePageTitle } from '@/composables/usePageTitle'
import { useTheme } from '@/composables/useTheme'
import { formatCount, formatDateTime } from '@/utils/format'
import { buildToc, mdHeadingId, sanitizeHtml } from '@/utils/markdown'
import type { TocItem } from '@/types/api'
import { showSuccess } from '@/utils/toast'
import LoadState from '@/components/LoadState.vue'
import TocSidebar from '@/components/TocSidebar.vue'
import CommentSection from '@/components/CommentSection.vue'
import AppIcon from '@/components/AppIcon.vue'

const route = useRoute()
const { theme } = useTheme()
usePageTitle(() => detail.value?.title || '文章详情')

const articleId = computed(() => {
  const id = Number(route.params.id)
  return Number.isInteger(id) && id > 0 ? id : null
})

const detail = ref<ArticleDetail | null>(null)
const loading = ref(false)
const loadError = ref('')

const tocItems = ref<TocItem[]>([])
const liked = ref(false)
const likeSubmitting = ref(false)

async function fetchDetail(): Promise<void> {
  if (!articleId.value) {
    loadError.value = '文章不存在或链接有误'
    return
  }
  loading.value = true
  loadError.value = ''
  detail.value = null
  tocItems.value = []
  try {
    const data = await getArticleDetail(articleId.value)
    detail.value = data
    liked.value = Boolean(data.liked)
  } catch (e) {
    loadError.value = e instanceof Error && e.message ? e.message : '文章加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 进入页面与路由参数变化（上一篇/下一篇跳转）时重新加载
watch(articleId, fetchDetail, { immediate: true })

const likeCount = computed(() => Number(detail.value?.likes ?? 0))
const viewCount = computed(() => Number(detail.value?.views ?? 0))
const commentCount = computed(() => Number(detail.value?.commentCount ?? 0))

async function handleLike(): Promise<void> {
  if (!articleId.value || likeSubmitting.value) return
  likeSubmitting.value = true
  try {
    const result = await toggleLike(articleId.value)
    liked.value = result.liked
    if (detail.value) detail.value.likes = result.likes
    showSuccess(result.liked ? '已点赞' : '已取消点赞')
  } catch {
    /* 拦截器已提示错误 */
  } finally {
    likeSubmitting.value = false
  }
}

function handleCatalog(list: unknown): void {
  tocItems.value = buildToc(list as { text: string; level: number }[])
}

async function submitComment(body: CommentPost): Promise<void> {
  if (!articleId.value) return
  await postComment(articleId.value, body)
  // 评论数本地 +1（下次进详情页会以后端为准）
  if (detail.value) detail.value.commentCount = commentCount.value + 1
}

const hasPrev = computed(() => Boolean(detail.value?.prevId))
const hasNext = computed(() => Boolean(detail.value?.nextId))
</script>

<template>
  <div class="article-detail">
    <LoadState
      :loading="loading"
      :error="loadError"
      skeleton
      @retry="fetchDetail"
    >
      <div v-if="detail" class="layout-with-toc">
        <article class="main-column">
          <!-- 文章头卡 -->
          <div class="card head-card">
            <h1 class="title">{{ detail.title }}</h1>

            <div class="meta">
              <span class="meta-item">
                <AppIcon name="calendar" :size="14" />
                {{ formatDateTime(detail.createTime) }}
              </span>
              <RouterLink v-if="detail.categoryId && detail.categoryName" :to="`/category/${detail.categoryId}`" class="meta-item category">
                <AppIcon name="folder" :size="14" />
                {{ detail.categoryName }}
              </RouterLink>
              <span class="meta-item">
                <AppIcon name="eye" :size="14" />
                {{ formatCount(viewCount) }} 阅读
              </span>
              <span class="meta-item">
                <AppIcon name="heart" :size="14" />
                {{ formatCount(likeCount) }} 点赞
              </span>
              <span class="meta-item">
                <AppIcon name="message-circle" :size="14" />
                {{ formatCount(commentCount) }} 评论
              </span>
              <span v-if="detail.isTop === 1" class="top-mark">
                <AppIcon name="arrow-up" :size="13" />
                置顶
              </span>
            </div>

            <div v-if="detail.tags && detail.tags.length" class="tags">
              <RouterLink v-for="tag in detail.tags" :key="tag.id" :to="`/tag/${tag.id}`" class="chip">
                <AppIcon name="tag" :size="12" />
                {{ tag.name }}
              </RouterLink>
            </div>
          </div>

          <!-- 正文 -->
          <div class="card content-card">
            <MdPreview
              v-if="detail.content"
              :model-value="detail.content"
              :theme="theme"
              preview-theme="github"
              code-theme="github"
              :md-heading-id="mdHeadingId"
              :sanitize="sanitizeHtml"
              @on-get-catalog="handleCatalog"
            />
            <div v-else class="empty-content">
              <AppIcon name="file-text" :size="30" />
              <p>本文暂无正文内容</p>
            </div>
          </div>

          <!-- 点赞 -->
          <div class="like-area">
            <button type="button" class="like-btn" :class="{ liked }" :disabled="likeSubmitting" @click="handleLike">
              <AppIcon name="heart" :size="18" :filled="liked" />
              <span>{{ liked ? '已点赞' : '点赞' }}</span>
              <strong>{{ formatCount(likeCount) }}</strong>
            </button>
          </div>

          <!-- 上一篇 / 下一篇 -->
          <div class="prev-next">
            <RouterLink v-if="hasPrev" :to="`/article/${detail.prevId}`" class="pn-card card card-hover prev">
              <span class="pn-label">
                <AppIcon name="chevron-left" :size="14" />
                上一篇
              </span>
              <span class="pn-title">{{ detail.prevTitle }}</span>
            </RouterLink>
            <span v-else class="pn-card card prev disabled">已是最新一篇</span>

            <RouterLink v-if="hasNext" :to="`/article/${detail.nextId}`" class="pn-card card card-hover next">
              <span class="pn-label">
                下一篇
                <AppIcon name="chevron-right" :size="14" />
              </span>
              <span class="pn-title">{{ detail.nextTitle }}</span>
            </RouterLink>
            <span v-else class="pn-card card next disabled">已是最后一篇</span>
          </div>

          <!-- 评论区 -->
          <CommentSection
            :fetch-list="() => getCommentTree(articleId ?? 0)"
            :submit="submitComment"
            title="评论"
            placeholder="发表你的看法…（昵称必填，邮箱选填）"
            empty-text="还没有评论，快来抢沙发吧"
          />
        </article>

        <!-- 右侧目录 -->
        <TocSidebar :items="tocItems" />
      </div>
    </LoadState>
  </div>
</template>

<style scoped>
.head-card,
.content-card {
  padding: 24px;
}

.head-card {
  margin-bottom: 16px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.4;
}

.meta {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-item.category {
  color: var(--color-primary);
}

.meta-item.category:hover {
  text-decoration: underline;
}

.top-mark {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--danger-soft);
  color: var(--color-danger);
  font-weight: 600;
}

.tags {
  margin-top: 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--color-text-tertiary);
  font-size: 14px;
}

/* 点赞区 */
.like-area {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 26px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  font-size: 15px;
  color: var(--color-text-secondary);
  box-shadow: var(--shadow-card);
  transition: all 0.25s ease;
}

.like-btn:hover:not(:disabled) {
  color: var(--color-danger);
  border-color: #fca5a5;
  transform: translateY(-2px);
}

.like-btn.liked {
  color: #fff;
  background: linear-gradient(135deg, #f43f5e, #ef4444);
  border-color: transparent;
}

.like-btn:disabled {
  opacity: 0.7;
}

/* 上一篇 / 下一篇 */
.prev-next {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 14px;
}

.pn-card {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.pn-card.next {
  text-align: right;
  align-items: flex-end;
}

.pn-card.disabled {
  color: var(--color-text-tertiary);
  font-size: 13px;
  justify-content: center;
  align-items: center;
  opacity: 0.7;
}

a.pn-card:hover .pn-title {
  color: var(--color-primary);
}

.pn-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.pn-title {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

@media (max-width: 768px) {
  .head-card,
  .content-card {
    padding: 18px 16px;
  }

  .title {
    font-size: 20px;
  }

  .prev-next {
    grid-template-columns: 1fr;
  }

  .pn-card.next {
    text-align: left;
    align-items: flex-start;
  }
}
</style>
