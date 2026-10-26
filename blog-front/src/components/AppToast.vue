<script setup lang="ts">
import { toastState } from '@/utils/toast'
</script>

<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="toast-wrap">
      <div v-for="item in toastState.list" :key="item.id" class="toast-item" :class="`toast-${item.type}`" role="alert">
        {{ item.message }}
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<style scoped>
.toast-wrap {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3000;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  max-width: min(420px, calc(100vw - 32px));
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  color: #fff;
  background: rgba(31, 35, 41, 0.92);
  box-shadow: var(--shadow-toast);
  word-break: break-all;
}

.toast-success {
  background: rgba(34, 197, 94, 0.94);
}

.toast-error {
  background: rgba(239, 68, 68, 0.94);
}

.toast-info {
  background: rgba(31, 35, 41, 0.92);
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
