<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ArticleCard, ArticleQuery, PageResult } from '@/types/api'
import { getArticleList } from '@/api/article'
import { useRequest } from '@/composables/useRequest'
import ArticleCardVue from './ArticleCard.vue'
import LoadState from './LoadState.vue'
import AppPagination from './AppPagination.vue'

const props = withDefaults(
  defineProps<{
    /** 查询条件（不含分页参数），变化时自动回到第一页 */
    query?: ArticleQuery
    pageSize?: number
  }>(),
  { query: () => ({}), pageSize: 10 }
)

const state = reactive<{ pageNum: number }>({ pageNum: 1 })

const { data, loading, error, run } = useRequest<PageResult<ArticleCard>>(() =>
  getArticleList({ ...props.query, pageNum: state.pageNum, pageSize: props.pageSize })
)

function fetchPage(pageNum: number): void {
  state.pageNum = pageNum
  run()
}

// 查询条件变化：重置页码并重新加载
watch(
  () => props.query,
  () => {
    state.pageNum = 1
    run()
  },
  { deep: true }
)
</script>

<template>
  <LoadState
    :loading="loading"
    :error="error"
    :empty="!loading && !error && (data?.records?.length ?? 0) === 0"
    empty-text="暂无相关文章"
    empty-icon="file-text"
    @retry="run"
  >
    <div class="article-list">
      <ArticleCardVue v-for="article in data?.records ?? []" :key="article.id" :article="article" />
    </div>

    <AppPagination
      v-if="data && data.total > 0"
      :page-num="state.pageNum"
      :page-size="props.pageSize"
      :total="data.total"
      @change="fetchPage"
    />
  </LoadState>
</template>

<style scoped>
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
