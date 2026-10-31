<script setup lang="ts">
import type { FriendLink } from '@/types/api'
import { getFriendLinkList } from '@/api/friendLink'
import { useRequest } from '@/composables/useRequest'
import { usePageTitle } from '@/composables/usePageTitle'
import { useMeta } from '@/composables/useMeta'
import LoadState from '@/components/LoadState.vue'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('友链')
useMeta({ description: '友情链接——值得一看的博客站点推荐' })
const { data, loading, error, run } = useRequest<FriendLink[]>(getFriendLinkList)

function initial(name: string): string {
  return name.charAt(0).toUpperCase()
}

function colorStyle(id: number): { background: string } {
  const colors = ['#4f6ef7', '#f59e0b', '#10b981', '#f43f5e', '#8b5cf6', '#0ea5e9', '#14b8a6', '#f97316']
  return { background: colors[Math.abs(id) % colors.length] }
}

function logoUrl(logo?: string | null): string {
  if (!logo) return ''
  if (/^https?:\/\//i.test(logo) || logo.startsWith('/')) return logo
  return `/${logo}`
}
</script>

<template>
  <div class="friends-page">
    <header class="page-header">
      <h1><AppIcon name="link" :size="20" /> 友情链接</h1>
      <p class="page-desc">共 {{ data?.length ?? 0 }} 个小伙伴，欢迎交换友链</p>
    </header>

    <LoadState :loading="loading" :error="error" :empty="!data?.length" empty-text="暂无友情链接" empty-icon="link" @retry="run">
      <div class="grid">
        <a
          v-for="friend in data ?? []"
          :key="friend.id"
          :href="friend.url"
          target="_blank"
          rel="noopener noreferrer"
          class="friend-card card card-hover"
        >
          <img
            v-if="logoUrl(friend.logo)"
            :src="logoUrl(friend.logo)"
            :alt="friend.name"
            class="logo"
            loading="lazy"
            @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
          />
          <div v-else class="logo logo-fallback" :style="colorStyle(friend.id)">{{ initial(friend.name) }}</div>

          <div class="info">
            <h3 class="name">
              {{ friend.name }}
              <AppIcon name="chevron-right" :size="13" />
            </h3>
            <p class="desc">{{ friend.description || '这个站点还没有介绍' }}</p>
          </div>
        </a>
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

.friend-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
}

.logo {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid var(--color-border);
  background: var(--color-card);
}

.logo-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
}

.info {
  min-width: 0;
}

.name {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 15.5px;
  font-weight: 600;
  transition: color 0.2s ease;
}

.friend-card:hover .name {
  color: var(--color-primary);
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
