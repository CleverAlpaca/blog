<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getUserHome, getUserArticles } from '@/api/userPage'
import { usePageTitle } from '@/composables/usePageTitle'
import type { ArticleCard, PageResult, UserHome } from '@/types/api'
import ArticleCardVue from '@/components/ArticleCard.vue'
import AppPagination from '@/components/AppPagination.vue'
import AppIcon from '@/components/AppIcon.vue'
import LoadState from '@/components/LoadState.vue'

const route = useRoute()
const userId = computed(() => Number(route.params.id))

const profile = ref<UserHome | null>(null)
const page = ref<PageResult<ArticleCard> | null>(null)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const error = ref(false)

usePageTitle(() => (profile.value ? `${profile.value.nickname} 的主页` : '个人主页'))

async function load(): Promise<void> {
  if (!Number.isInteger(userId.value)) {
    error.value = true
    return
  }
  loading.value = true
  try {
    page.value = await getUserArticles(userId.value, { pageNum: pageNum.value, pageSize })
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number): void {
  pageNum.value = page
  void load()
}

onMounted(async () => {
  loading.value = true
  try {
    profile.value = await getUserHome(userId.value)
    await load()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="user-home-page">
    <LoadState :loading="loading && !profile" :error="error && !profile" @retry="load">
      <div v-if="profile" class="card profile-card">
        <img
          v-if="profile.avatar"
          :src="profile.avatar"
          :alt="profile.nickname"
          class="avatar"
          @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
        />
        <div v-else class="avatar avatar-fallback">
          <AppIcon name="user" :size="30" />
        </div>
        <div class="profile-info">
          <h1 class="nickname">
            {{ profile.nickname }}
            <span class="badge" :class="profile.badge">{{ profile.badge === 'admin' ? '博主' : '作者' }}</span>
          </h1>
          <p class="intro">{{ profile.intro || '这位作者还没有写简介' }}</p>
          <p class="reg">加入于 {{ profile.regDate }}</p>
        </div>
      </div>

      <div class="section-title">
        <AppIcon name="file-text" :size="16" />
        已发布的文章（{{ page?.total ?? 0 }}）
      </div>

      <LoadState :loading="loading" :error="error" :empty="(page?.records?.length ?? 0) === 0" empty-text="还没有已发布的文章">
        <div class="article-list">
          <ArticleCardVue v-for="a in page?.records ?? []" :key="a.id" :article="a" />
        </div>
        <AppPagination
          v-if="(page?.total ?? 0) > pageSize"
          :page-num="pageNum"
          :page-size="pageSize"
          :total="page?.total ?? 0"
          @change="onPageChange"
        />
      </LoadState>
    </LoadState>
  </div>
</template>

<style scoped>
.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 20px;
}

.avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--color-card);
  box-shadow: var(--shadow-card);
  flex-shrink: 0;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.nickname {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge.admin {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.badge.user {
  background: var(--color-chip);
  color: var(--color-text-secondary);
}

.intro {
  margin-top: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.reg {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 18px;
}
</style>
