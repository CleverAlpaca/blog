<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { deleteMyArticle, getMyArticles, getMyStats } from '@/api/my'
import { usePageTitle } from '@/composables/usePageTitle'
import type { ArticleCard, MyArticleStats } from '@/types/api'
import { formatDateTime } from '@/utils/format'
import AppPagination from '@/components/AppPagination.vue'
import AppIcon from '@/components/AppIcon.vue'
import { showSuccess } from '@/utils/toast'

usePageTitle('我的投稿')

const router = useRouter()
const stats = ref<MyArticleStats | null>(null)
const list = ref<ArticleCard[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const status = ref<number | undefined>(undefined)
const loading = ref(false)

const tabs = computed(() => [
  { label: '全部', value: undefined, count: sum() },
  { label: '草稿', value: 0, count: stats.value?.draft ?? 0 },
  { label: '待审核', value: 3, count: stats.value?.pending ?? 0 },
  { label: '已退回', value: 4, count: stats.value?.returned ?? 0 },
  { label: '已发布', value: 1, count: stats.value?.published ?? 0 }
])

function sum(): number {
  const s = stats.value
  return s ? s.draft + s.pending + s.returned + s.published : 0
}

function statusLabel(value: number): string {
  return value === 0 ? '草稿' : value === 3 ? '待审核' : value === 4 ? '已退回' : '已发布'
}

function statusClass(value: number): string {
  return value === 0 ? 's-draft' : value === 3 ? 's-pending' : value === 4 ? 's-returned' : 's-published'
}

async function load(): Promise<void> {
  loading.value = true
  try {
    const [page, st] = await Promise.all([
      getMyArticles({ pageNum: pageNum.value, pageSize, status: status.value }),
      getMyStats()
    ])
    list.value = page.records
    total.value = page.total
    stats.value = st
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function switchTab(value?: number): void {
  status.value = value
  pageNum.value = 1
  void load()
}

function edit(id: number): void {
  router.push(`/write?id=${id}`)
}

async function remove(id: number): Promise<void> {
  try {
    await deleteMyArticle(id)
    showSuccess('已删除')
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

function onPageChange(page: number): void {
  pageNum.value = page
  void load()
}

onMounted(load)
</script>

<template>
  <div class="my-articles-page">
    <header class="page-header">
      <h1>我的投稿</h1>
      <p class="page-desc">管理自己发布的文章与投稿；点击「写文章」开始新投稿</p>
    </header>

    <div class="toolbar">
      <button
        v-for="tab in tabs"
        :key="tab.label"
        type="button"
        class="tab-btn"
        :class="{ active: status === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
        <span class="tab-count">{{ tab.count }}</span>
      </button>
      <RouterLink to="/write" class="btn btn-primary new-btn">
        <AppIcon name="send" :size="14" />
        写文章
      </RouterLink>
    </div>

    <div v-if="!loading && list.length === 0" class="card empty-card">
      <AppIcon name="inbox" :size="28" />
      <p>这里还没有文章</p>
    </div>

    <div class="list-wrap">
      <div v-for="a in list" :key="a.id" class="card row-card">
        <div class="row-main">
          <div class="row-head">
            <span class="status" :class="statusClass(a.status)">{{ statusLabel(a.status) }}</span>
            <RouterLink :to="a.status === 1 ? `/article/${a.id}` : `/write?id=${a.id}`" class="row-title">
              {{ a.title }}
            </RouterLink>
          </div>
          <p v-if="a.status === 4" class="reject-line">退回原因：{{ a.rejectReason || '未填写' }}</p>
          <div class="row-meta">
            <span>{{ formatDateTime(a.updateTime) }} 更新</span>
            <span v-if="a.status === 1">{{ a.views }} 阅读 · {{ a.likes }} 点赞 · {{ a.commentCount }} 评论</span>
          </div>
        </div>
        <div class="row-actions">
          <button v-if="a.status === 0 || a.status === 4" type="button" class="row-btn" @click="edit(a.id)">
            编辑
          </button>
          <button type="button" class="row-btn danger" @click="remove(a.id)">删除</button>
        </div>
      </div>
    </div>

    <AppPagination
      v-if="total > pageSize"
      :page-num="pageNum"
      :page-size="pageSize"
      :total="total"
      @change="onPageChange"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tab-btn {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  font-size: 13.5px;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  color: var(--color-primary);
  font-weight: 600;
}

.tab-count {
  margin-left: 4px;
  font-size: 12px;
  opacity: 0.8;
}

.new-btn {
  margin-left: auto;
}

.empty-card {
  padding: 48px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--color-text-tertiary);
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}

.row-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
}

.row-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.row-title {
  font-weight: 600;
  color: var(--color-text);
}

.row-title:hover {
  color: var(--color-primary);
}

.status {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.s-draft {
  background: var(--color-chip);
  color: var(--color-text-secondary);
}

.s-pending {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.s-returned {
  background: var(--danger-soft);
  color: var(--color-danger);
}

.reject-line {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-danger);
}

.row-meta {
  margin-top: 6px;
  display: flex;
  gap: 14px;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.row-btn {
  padding: 5px 12px;
  border-radius: var(--radius-small);
  border: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
}

.row-btn:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.row-btn.danger:hover {
  color: var(--color-danger);
  border-color: var(--color-danger);
}
</style>
