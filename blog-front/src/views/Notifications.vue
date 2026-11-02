<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notify'
import { useNotifyStore } from '@/stores/notify'
import { usePageTitle } from '@/composables/usePageTitle'
import type { NotificationItem } from '@/types/api'
import { formatDateTime } from '@/utils/format'
import AppPagination from '@/components/AppPagination.vue'
import AppIcon from '@/components/AppIcon.vue'
import LoadState from '@/components/LoadState.vue'

usePageTitle('通知中心')

const router = useRouter()
const notifyStore = useNotifyStore()

const list = ref<NotificationItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const error = ref(false)

async function load(): Promise<void> {
  loading.value = true
  try {
    const page = await getNotifications({ pageNum: pageNum.value, pageSize })
    list.value = page.records
    total.value = page.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function open(item: NotificationItem): Promise<void> {
  try {
    if (item.isRead === 0) {
      await markNotificationRead(item.id)
      item.isRead = 1
      void notifyStore.refresh()
    }
  } catch {
    /* 已读失败不影响跳转 */
  }
  if (item.link) {
    router.push(item.link)
  }
}

async function readAll(): Promise<void> {
  try {
    await markAllNotificationsRead()
    list.value = list.value.map((n) => ({ ...n, isRead: 1 }))
    void notifyStore.refresh()
  } catch {
    /* 拦截器已提示 */
  }
}

function typeIcon(type: string): string {
  return type === 'message' ? 'mail' : 'message-circle'
}

function onPageChange(page: number): void {
  pageNum.value = page
  void load()
}

onMounted(load)
</script>

<template>
  <div class="notifications-page">
    <div class="page-head">
      <h1 class="page-title">通知中心</h1>
      <button
        v-if="list.some((n) => n.isRead === 0)"
        type="button"
        class="btn btn-ghost read-all-btn"
        @click="readAll"
      >
        全部已读
      </button>
    </div>

    <LoadState
      :loading="loading"
      :error="error"
      :empty="total === 0"
      empty-text="还没有收到通知"
      empty-icon="bell"
      @retry="load"
    >
      <div class="notice-list">
        <button
          v-for="n in list"
          :key="n.id"
          type="button"
          class="notice-item card"
          :class="{ unread: n.isRead === 0 }"
          @click="open(n)"
        >
          <span class="type-icon" :class="n.type">
            <AppIcon :name="typeIcon(n.type)" :size="16" />
          </span>
          <span class="notice-main">
            <span class="notice-title">
              <span v-if="n.isRead === 0" class="unread-dot"></span>
              {{ n.title }}
            </span>
            <span class="notice-content">{{ n.content }}</span>
          </span>
          <span class="notice-time">{{ formatDateTime(n.createTime) }}</span>
        </button>
      </div>
      <AppPagination
        v-if="total > pageSize"
        :page-num="pageNum"
        :page-size="pageSize"
        :total="total"
        @change="onPageChange"
      />
    </LoadState>
  </div>
</template>

<style scoped>
.notifications-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0 32px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
}

.read-all-btn {
  font-size: 13px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-small);
  background: var(--color-card);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.notice-item:hover {
  border-color: var(--color-primary);
  transform: translateY(-1px);
}

.notice-item.unread {
  background: var(--color-primary-soft);
}

.type-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--color-chip);
  color: var(--color-text-secondary);
}

.type-icon.comment {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.notice-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.unread-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-danger);
}

.notice-content {
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notice-time {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
