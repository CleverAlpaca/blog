<script setup lang="ts">
import { usePageTitle } from '@/composables/usePageTitle'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('页面不存在')
</script>

<template>
  <div class="not-found">
    <div class="inner card">
      <p class="code">404</p>
      <h1 class="title">页面走丢了</h1>
      <p class="desc">你访问的页面不存在或已被移除</p>
      <RouterLink to="/" class="btn btn-primary">
        <AppIcon name="home" :size="15" />
        返回首页
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  padding: 60px 0 40px;
}

.inner {
  width: 100%;
  max-width: 460px;
  padding: 56px 32px;
  text-align: center;
}

.code {
  font-family: var(--font-mono);
  font-size: 72px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #4f6ef7, #9a5cf0);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.title {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 700;
}

.desc {
  margin: 8px 0 22px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
