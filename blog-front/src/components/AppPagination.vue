<script setup lang="ts">
import { computed } from 'vue'
import AppIcon from './AppIcon.vue'

const props = withDefaults(
  defineProps<{
    pageNum: number
    pageSize: number
    total: number
    /** 页码按钮前后各显示几个 */
    siblings?: number
  }>(),
  { siblings: 2 }
)

const emit = defineEmits<{ change: [page: number] }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / Math.max(1, props.pageSize))))

const visiblePages = computed<(number | '…')[]>(() => {
  const count = totalPages.value
  const current = props.pageNum
  const { siblings } = props
  if (count <= siblings * 2 + 3) {
    return Array.from({ length: count }, (_, i) => i + 1)
  }
  const pages: (number | '…')[] = [1]
  const start = Math.max(2, current - siblings)
  const end = Math.min(count - 1, current + siblings)
  if (start > 2) pages.push('…')
  for (let i = start; i <= end; i++) pages.push(i)
  if (end < count - 1) pages.push('…')
  pages.push(count)
  return pages
})

function go(page: number): void {
  if (page < 1 || page > totalPages.value || page === props.pageNum) return
  emit('change', page)
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="分页">
    <button class="page-btn" type="button" :disabled="pageNum <= 1" aria-label="上一页" @click="go(pageNum - 1)">
      <AppIcon name="chevron-left" :size="15" />
    </button>

    <template v-for="(page, index) in visiblePages" :key="`${page}-${index}`">
      <span v-if="page === '…'" class="page-ellipsis">…</span>
      <button v-else class="page-btn" :class="{ active: page === pageNum }" type="button" @click="go(page)">
        {{ page }}
      </button>
    </template>

    <button class="page-btn" type="button" :disabled="pageNum >= totalPages" aria-label="下一页" @click="go(pageNum + 1)">
      <AppIcon name="chevron-right" :size="15" />
    </button>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 24px;
}

.page-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-small);
  border: 1px solid var(--color-border);
  background: var(--color-card);
  color: var(--color-text-secondary);
  font-size: 14px;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled):not(.active) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.page-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

.page-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.page-ellipsis {
  color: var(--color-text-tertiary);
  padding: 0 2px;
}
</style>
