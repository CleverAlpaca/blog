<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ArticleList from '@/components/ArticleList.vue'
import SiteSidebar from '@/components/SiteSidebar.vue'
import { usePageTitle } from '@/composables/usePageTitle'
import { useVisitorId } from '@/composables/useVisitorId'

const route = useRoute()

usePageTitle()
// 初始化访客标识（后续请求由 axios 拦截器统一携带）
useVisitorId()

const query = computed(() => ({
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined
}))

const searching = computed(() => Boolean(query.value.keyword))
</script>

<template>
  <div class="home-page">
    <div v-if="searching" class="search-banner card">
      关键词「<strong>{{ query.keyword }}</strong>」的搜索结果
    </div>

    <div class="layout-with-sidebar">
      <div class="main-column">
        <ArticleList :query="query" :page-size="10" />
      </div>
      <SiteSidebar />
    </div>
  </div>
</template>

<style scoped>
.search-banner {
  padding: 12px 18px;
  margin-bottom: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.search-banner strong {
  color: var(--color-primary);
}
</style>
