<script setup lang="ts">
import type { ArchiveYear } from '@/types/api'
import { getArchives } from '@/api/article'
import { useRequest } from '@/composables/useRequest'
import { usePageTitle } from '@/composables/usePageTitle'
import { useMeta } from '@/composables/useMeta'
import { formatShortDate } from '@/utils/format'
import LoadState from '@/components/LoadState.vue'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('归档')
useMeta({ description: '文章归档时间轴，按年份浏览全部博客文章' })
const { data, loading, error, run } = useRequest<ArchiveYear[]>(getArchives)

const total = (): number => (data.value ?? []).reduce((sum, y) => sum + (y.articles?.length ?? 0), 0)
</script>

<template>
  <div class="archive-page">
    <header class="page-header">
      <h1><AppIcon name="archive" :size="20" /> 归档</h1>
      <p class="page-desc">共 {{ total() }} 篇文章，按发布时间倒序</p>
    </header>

    <div class="card timeline-card">
      <LoadState :loading="loading" :error="error" :empty="!data?.length" empty-text="暂无文章" empty-icon="archive" @retry="run">
        <div class="timeline">
          <section v-for="yearGroup in data ?? []" :key="yearGroup.year" class="year-group">
            <div class="year-node">
              <span class="year">{{ yearGroup.year }}</span>
              <span class="year-count">{{ yearGroup.articles?.length ?? 0 }} 篇</span>
            </div>

            <ul class="items">
              <li v-for="article in yearGroup.articles ?? []" :key="article.id" class="item">
                <span class="dot"></span>
                <span class="date">{{ formatShortDate(article.createTime) }}</span>
                <RouterLink :to="`/article/${article.id}`" class="title">{{ article.title }}</RouterLink>
              </li>
            </ul>
          </section>
        </div>
      </LoadState>
    </div>
  </div>
</template>

<style scoped>
.timeline-card {
  padding: 24px 20px;
}

.timeline {
  position: relative;
}

.year-group {
  position: relative;
  padding-left: 24px;
}

/* 主时间轴线 */
.year-group::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 8px;
  bottom: -8px;
  width: 2px;
  background: var(--timeline-line);
}

.year-group:last-child::before {
  bottom: 16px;
}

.year-node {
  display: flex;
  align-items: baseline;
  gap: 10px;
  position: relative;
  margin-bottom: 8px;
}

.year-node::before {
  content: '';
  position: absolute;
  left: -24px;
  top: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 0 4px var(--color-primary-soft);
}

.year {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
}

.year-count {
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.items {
  padding: 4px 0 20px;
}

.item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 9px 6px;
  border-radius: var(--radius-small);
  transition: background 0.2s ease;
}

.item:hover {
  background: var(--color-chip);
}

.item .dot {
  position: absolute;
  left: -22.5px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--timeline-dot);
  transition: background 0.2s ease, transform 0.2s ease;
}

.item:hover .dot {
  background: var(--color-primary);
  transform: scale(1.3);
}

.date {
  flex-shrink: 0;
  font-family: var(--font-mono);
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.title {
  font-size: 14.5px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

.title:hover {
  color: var(--color-primary);
}
</style>
