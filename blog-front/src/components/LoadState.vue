<script setup lang="ts">
import AppIcon from './AppIcon.vue'

/**
 * 统一异步状态容器：loading → 骨架/spinner；error → 错误 + 重试；
 * empty → 空状态；否则渲染默认插槽内容。
 */
withDefaults(
  defineProps<{
    loading?: boolean
    error?: string
    /** 是否为空状态（error 优先于 empty） */
    empty?: boolean
    emptyText?: string
    emptyIcon?: string
    /** loading 时显示骨架屏（否则显示 spinner） */
    skeleton?: boolean
  }>(),
  { loading: false, error: '', empty: false, emptyText: '暂无数据', emptyIcon: 'inbox', skeleton: false }
)

const emit = defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="loading" class="state-wrap">
    <template v-if="skeleton">
      <div class="skeleton skeleton-line w60"></div>
      <div class="skeleton skeleton-line w100"></div>
      <div class="skeleton skeleton-line w90"></div>
      <div class="skeleton skeleton-line w40"></div>
    </template>
    <div v-else class="spinner"></div>
    <p v-if="!skeleton" class="state-text">加载中…</p>
  </div>

  <div v-else-if="error" class="state-wrap state-error">
    <AppIcon name="alert-circle" :size="34" />
    <p class="state-text">{{ error }}</p>
    <button class="btn btn-ghost" type="button" @click="emit('retry')">重新加载</button>
  </div>

  <div v-else-if="empty" class="state-wrap">
    <AppIcon :name="emptyIcon" :size="34" />
    <p class="state-text">{{ emptyText }}</p>
    <slot name="empty-action" />
  </div>

  <slot v-else />
</template>

<style scoped>
.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 16px;
  color: var(--color-text-tertiary);
  min-height: 180px;
}

.state-error {
  color: var(--color-danger);
}

.state-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.skeleton-line {
  height: 14px;
}

.w100 {
  width: 100%;
}

.w90 {
  width: 90%;
}

.w60 {
  width: 60%;
}

.w40 {
  width: 40%;
}
</style>
