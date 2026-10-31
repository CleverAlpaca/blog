<script setup lang="ts">
import type { Tag } from '@/types/api'
import { getTagList } from '@/api/tag'
import { useRequest } from '@/composables/useRequest'
import { usePageTitle } from '@/composables/usePageTitle'
import { useMeta } from '@/composables/useMeta'
import LoadState from '@/components/LoadState.vue'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('标签')
useMeta({ description: '按标签浏览博客文章' })
const { data, loading, error, run } = useRequest<Tag[]>(getTagList)

/** 字号随文章数轻微浮动，形成标签云观感 */
function sizeClass(count?: number | null): string {
  const value = count ?? 0
  if (value >= 5) return 'size-lg'
  if (value >= 2) return 'size-md'
  return 'size-sm'
}
</script>

<template>
  <div class="tags-page">
    <header class="page-header">
      <h1><AppIcon name="tag" :size="20" /> 标签</h1>
      <p class="page-desc">共 {{ data?.length ?? 0 }} 个标签</p>
    </header>

    <div class="card cloud-card">
      <LoadState :loading="loading" :error="error" :empty="!data?.length" empty-text="暂无标签" empty-icon="tag" @retry="run">
        <div class="cloud">
          <RouterLink v-for="tag in data ?? []" :key="tag.id" :to="`/tag/${tag.id}`" class="cloud-tag" :class="sizeClass(tag.count)">
            <AppIcon name="tag" :size="13" />
            {{ tag.name }}
            <span class="num">{{ tag.count ?? 0 }}</span>
          </RouterLink>
        </div>
      </LoadState>
    </div>
  </div>
</template>

<style scoped>
.cloud-card {
  padding: 28px 24px;
}

.cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.cloud-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  transition: all 0.22s ease;
}

.cloud-tag:hover {
  color: #fff;
  background: var(--color-primary);
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(79, 110, 247, 0.28);
}

.cloud-tag:hover .num {
  color: rgba(255, 255, 255, 0.85);
}

.size-lg {
  font-size: 16.5px;
  font-weight: 600;
}

.size-md {
  font-size: 14.5px;
}

.size-sm {
  font-size: 13.5px;
}

.num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--color-chip);
  color: var(--color-text-tertiary);
  font-size: 11.5px;
}
</style>
