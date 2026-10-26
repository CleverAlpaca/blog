<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import type { Category } from '@/types/api'
import { getCategoryList } from '@/api/category'
import { usePageTitle } from '@/composables/usePageTitle'
import ArticleList from '@/components/ArticleList.vue'
import AppIcon from '@/components/AppIcon.vue'

const route = useRoute()
usePageTitle(() => `分类：${category.value?.name || ''}`)

const categoryId = computed(() => Number(route.params.id))
const query = computed(() => (Number.isInteger(categoryId.value) ? { categoryId: categoryId.value } : {}))

const { data: categories, run } = useRequest<Category[]>(getCategoryList)
watch(categoryId, () => run(), { immediate: true })

const category = computed(() => (categories.value ?? []).find((c) => c.id === categoryId.value))
</script>

<template>
  <div class="category-detail-page">
    <header class="page-header">
      <h1>
        <AppIcon name="folder" :size="20" />
        分类：<span class="accent">{{ category?.name || '加载中…' }}</span>
      </h1>
      <p v-if="category?.description" class="page-desc">{{ category.description }}</p>
    </header>

    <ArticleList :query="query" :page-size="10" />
  </div>
</template>
