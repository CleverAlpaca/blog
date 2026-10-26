<template>
  <div class="page-container" v-loading="loading">
    <!-- 统计卡片网格 -->
    <div class="stat-grid">
      <el-card v-for="item in statCards" :key="item.label" shadow="hover" class="stat-card">
        <div class="stat-card-body">
          <div class="stat-icon" :style="{ backgroundColor: item.bg, color: item.color }">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ item.value.toLocaleString() }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最新评论 -->
    <el-card shadow="never" class="latest-card">
      <template #header>
        <span class="card-title">最新评论</span>
      </template>
      <el-table :data="dashboard?.latestComments || []" style="width: 100%" empty-text="暂无评论">
        <el-table-column prop="nickname" label="昵称" width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDashboard } from '@/api/dashboard'
import { formatDateTime } from '@/utils/format'
import type { DashboardData } from '@/api/types'

const loading = ref(false)
const dashboard = ref<DashboardData | null>(null)

const statCards = computed(() => [
  { label: '发布文章', value: dashboard.value?.publishedCount ?? 0, icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
  { label: '草稿文章', value: dashboard.value?.draftCount ?? 0, icon: 'EditPen', color: '#909399', bg: '#f4f4f5' },
  { label: '评论总数', value: dashboard.value?.commentCount ?? 0, icon: 'ChatDotRound', color: '#67c23a', bg: '#f0f9eb' },
  { label: '留言总数', value: dashboard.value?.messageCount ?? 0, icon: 'ChatLineSquare', color: '#e6a23c', bg: '#fdf6ec' },
  { label: '总浏览量', value: dashboard.value?.totalViews ?? 0, icon: 'View', color: '#f56c6c', bg: '#fef0f0' },
  { label: '总点赞数', value: dashboard.value?.totalLikes ?? 0, icon: 'Star', color: '#f56c6c', bg: '#fef0f0' },
  { label: '分类数量', value: dashboard.value?.categoryCount ?? 0, icon: 'FolderOpened', color: '#409eff', bg: '#ecf5ff' },
  { label: '标签数量', value: dashboard.value?.tagCount ?? 0, icon: 'PriceTag', color: '#67c23a', bg: '#f0f9eb' },
  { label: '友链数量', value: dashboard.value?.friendLinkCount ?? 0, icon: 'Link', color: '#909399', bg: '#f4f4f5' }
])

onMounted(async () => {
  loading.value = true
  try {
    dashboard.value = await getDashboard()
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  min-width: 0;
}

.stat-label {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.latest-card .card-title {
  font-weight: 600;
}
</style>
