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

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">热门文章 Top 5（浏览量）</span>
          </template>
          <div v-if="(dashboard?.topArticles || []).length" ref="topChartRef" class="chart-box"></div>
          <el-empty v-else description="暂无文章数据" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">分类文章分布</span>
          </template>
          <div v-if="(dashboard?.categoryDistribution || []).length" ref="pieChartRef" class="chart-box"></div>
          <el-empty v-else description="暂无文章数据" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

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
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboard } from '@/api/dashboard'
import { formatDateTime } from '@/utils/format'
import type { DashboardData } from '@/api/types'

// ECharts 按需注册（本页只用条形图/饼图/网格/提示框/图例）
echarts.use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const loading = ref(false)
const dashboard = ref<DashboardData | null>(null)

const topChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
let topChart: ReturnType<typeof echarts.init> | null = null
let pieChart: ReturnType<typeof echarts.init> | null = null

const statCards = computed(() => [
  { label: '发布文章', value: dashboard.value?.publishedCount ?? 0, icon: 'Document', color: '#409eff', bg: '#ecf5ff' },
  { label: '草稿文章', value: dashboard.value?.draftCount ?? 0, icon: 'EditPen', color: '#909399', bg: '#f4f4f5' },
  { label: '评论总数', value: dashboard.value?.commentCount ?? 0, icon: 'ChatDotRound', color: '#67c23a', bg: '#f0f9eb' },
  { label: '留言总数', value: dashboard.value?.messageCount ?? 0, icon: 'ChatLineSquare', color: '#e6a23c', bg: '#fdf6ec' },
  { label: '总浏览量', value: dashboard.value?.totalViews ?? 0, icon: 'View', color: '#f56c6c', bg: '#fef0f0' },
  { label: '总点赞数', value: dashboard.value?.totalLikes ?? 0, icon: 'Star', color: '#f56c6c', bg: '#fef0f0' },
  { label: '分类数量', value: dashboard.value?.categoryCount ?? 0, icon: 'FolderOpened', color: '#409eff', bg: '#ecf5ff' },
  { label: '标签数量', value: dashboard.value?.tagCount ?? 0, icon: 'PriceTag', color: '#67c23a', bg: '#f0f9eb' },
  { label: '友链数量', value: dashboard.value?.friendLinkCount ?? 0, icon: 'Link', color: '#909399', bg: '#f4f4f5' },
  { label: '注册用户', value: dashboard.value?.userCount ?? 0, icon: 'User', color: '#67c23a', bg: '#f0f9eb' }
])

const renderCharts = (): void => {
  if (!dashboard.value) return
  const topList = dashboard.value.topArticles || []
  if (topList.length && topChartRef.value) {
    topChart = topChart ?? echarts.init(topChartRef.value)
    topChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 8, right: 28, top: 10, bottom: 10, containLabel: true },
      xAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { type: 'dashed' } } },
      yAxis: {
        type: 'category',
        inverse: true,
        data: topList.map(a => a.title),
        axisLabel: { width: 110, overflow: 'truncate' }
      },
      series: [{
        name: '浏览量',
        type: 'bar',
        data: topList.map(a => a.views),
        barMaxWidth: 16,
        itemStyle: { color: '#409eff', borderRadius: [0, 6, 6, 0] }
      }]
    })
  }
  const distList = dashboard.value.categoryDistribution || []
  if (distList.length && pieChartRef.value) {
    pieChart = pieChart ?? echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}：{c} 篇（{d}%）' },
      legend: { bottom: 0, icon: 'circle' },
      series: [{
        name: '分类文章分布',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: distList.map(c => ({ name: c.name, value: c.count }))
      }]
    })
  }
}

const handleResize = (): void => {
  topChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  loading.value = true
  try {
    dashboard.value = await getDashboard()
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
})

// 数据返回后等 DOM 渲染出图表容器再画图
watch(dashboard, () => nextTick(renderCharts))

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  topChart?.dispose()
  pieChart?.dispose()
  topChart = null
  pieChart = null
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

.chart-row {
  margin-bottom: 16px;
}

.chart-box {
  height: 300px;
  width: 100%;
}

.latest-card .card-title {
  font-weight: 600;
}
</style>
