<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { TocItem } from '@/types/api'
import AppIcon from './AppIcon.vue'

const props = defineProps<{ items: TocItem[] }>()

const activeId = ref('')
let ticking = false

/** scroll spy：定位当前视口中最近的标题 */
function updateActive(): void {
  ticking = false
  if (!props.items.length) {
    activeId.value = ''
    return
  }
  const offset = 90
  let current = ''
  for (const item of props.items) {
    const el = document.getElementById(item.id)
    if (!el) continue
    if (el.getBoundingClientRect().top <= offset) {
      current = item.id
    } else {
      break
    }
  }
  // 滚动到顶部以上时取第一个可见项
  if (!current && props.items.length) {
    const first = props.items.map((i) => document.getElementById(i.id)).find(Boolean)
    current = first?.id ?? ''
  }
  activeId.value = current
}

function onScroll(): void {
  if (!ticking) {
    ticking = true
    window.requestAnimationFrame(updateActive)
  }
}

function scrollTo(item: TocItem): void {
  const el = document.getElementById(item.id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeId.value = item.id
  }
}

watch(
  () => props.items,
  () => {
    activeId.value = props.items[0]?.id ?? ''
    window.requestAnimationFrame(updateActive)
  },
  { immediate: true }
)

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <aside v-if="items.length" class="toc-column card">
    <h3 class="toc-header">
      <AppIcon name="archive" :size="14" />
      目录
    </h3>
    <nav class="toc-list">
      <button
        v-for="item in items"
        :key="item.id"
        type="button"
        class="toc-item"
        :class="[`level-${item.level}`, { active: item.id === activeId }]"
        :title="item.text"
        @click="scrollTo(item)"
      >
        {{ item.text }}
      </button>
    </nav>
  </aside>
</template>

<style scoped>
.toc-column {
  position: sticky;
  top: calc(var(--header-height) + 16px);
  padding: 14px 14px;
  max-height: calc(100vh - var(--header-height) - 32px);
  overflow-y: auto;
}

.toc-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 2px 12px 8px;
  border-bottom: 1px solid var(--color-border);
}

.toc-list {
  padding-top: 6px;
}
</style>
