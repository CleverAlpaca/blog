<script setup lang="ts">
import type { Category } from '@/types/api'
import { getCategoryList } from '@/api/category'
import { useRequest } from '@/composables/useRequest'
import { usePageTitle } from '@/composables/usePageTitle'
import LoadState from '@/components/LoadState.vue'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('分类')
const { data, loading, error, run } = useRequest<Category[]>(getCategoryList)
</script>

<template>
  <div class="categories-page">
    <header class="page-header">
      <h1><AppIcon name="folder" :size="20" /> 分类</h1>
      <p class="page-desc">共 {{ data?.length ?? 0 }} 个分类</p>
    </header>

    <LoadState :loading="loading" :error="error" :empty="!data?.length" empty-text="暂无分类" empty-icon="folder" @retry="run">
      <div class="grid">
        <RouterLink v-for="category in data ?? []" :key="category.id" :to="`/category/${category.id}`" class="category-card card card-hover">
          <div class="icon-box">
            <AppIcon name="folder" :size="20" />
          </div>
          <div class="info">
            <h3 class="name">
              {{ category.name }}
              <span class="count">{{ category.articleCount ?? 0 }}</span>
            </h3>
            <p class="desc">{{ category.description || '查看该分类下的全部文章' }}</p>
          </div>
        </RouterLink>
      </div>
    </LoadState>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 560px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

.category-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
}

.icon-box {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.info {
  min-width: 0;
}

.name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  background: #f1f3f8;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
