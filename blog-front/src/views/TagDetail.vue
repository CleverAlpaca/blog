<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import type { Tag } from '@/types/api'
import { getTagList } from '@/api/tag'
import { usePageTitle } from '@/composables/usePageTitle'
import ArticleList from '@/components/ArticleList.vue'
import AppIcon from '@/components/AppIcon.vue'

const route = useRoute()
usePageTitle(() => `标签：${tag.value?.name || ''}`)

const tagId = computed(() => Number(route.params.id))
const query = computed(() => (Number.isInteger(tagId.value) ? { tagId: tagId.value } : {}))

const { data: tags, run } = useRequest<Tag[]>(getTagList)
watch(tagId, () => run(), { immediate: true })

const tag = computed(() => (tags.value ?? []).find((t) => t.id === tagId.value))
</script>

<template>
  <div class="tag-detail-page">
    <header class="page-header">
      <h1>
        <AppIcon name="tag" :size="20" />
        标签：<span class="accent">{{ tag?.name || '加载中…' }}</span>
      </h1>
      <p class="page-desc">该标签下的全部文章</p>
    </header>

    <ArticleList :query="query" :page-size="10" />
  </div>
</template>
