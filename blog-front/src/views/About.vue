<script setup lang="ts">
import type { About } from '@/types/api'
import { getAbout } from '@/api/site'
import { useRequest } from '@/composables/useRequest'
import { usePageTitle } from '@/composables/usePageTitle'
import { mdHeadingId, sanitizeHtml } from '@/utils/markdown'
import LoadState from '@/components/LoadState.vue'
import AppIcon from '@/components/AppIcon.vue'
import { MdPreview } from 'md-editor-v3'

usePageTitle('关于')
const { data, loading, error, run } = useRequest<About>(getAbout)

function avatarUrl(avatar?: string | null): string {
  if (!avatar) return ''
  if (/^https?:\/\//i.test(avatar) || avatar.startsWith('/')) return avatar
  return `/${avatar}`
}
</script>

<template>
  <div class="about-page">
    <header class="page-header">
      <h1><AppIcon name="user" :size="20" /> 关于</h1>
      <p class="page-desc">关于博主与这个站点</p>
    </header>

    <LoadState :loading="loading" :error="error" :empty="!data" empty-text="暂无关于信息" empty-icon="user" @retry="run">
      <div v-if="data" class="about-body">
        <!-- 博主信息卡 -->
        <div class="card profile-card">
          <img
            v-if="avatarUrl(data.avatar)"
            :src="avatarUrl(data.avatar)"
            :alt="data.nickname"
            class="avatar"
            @error="($event.target as HTMLImageElement).style.visibility = 'hidden'"
          />
          <div v-else class="avatar avatar-fallback">
            <AppIcon name="user" :size="34" />
          </div>

          <div class="profile-info">
            <h2 class="nickname">{{ data.nickname }}</h2>
            <p v-if="data.intro" class="intro">{{ data.intro }}</p>
            <a v-if="data.email" :href="`mailto:${data.email}`" class="email">
              <AppIcon name="mail" :size="14" />
              {{ data.email }}
            </a>
          </div>
        </div>

        <!-- 「关于我」Markdown 内容（可能为 null） -->
        <div v-if="data.content" class="card content-card">
          <MdPreview
            :model-value="data.content"
            theme="light"
            preview-theme="github"
            code-theme="github"
            :md-heading-id="mdHeadingId"
            :sanitize="sanitizeHtml"
          />
        </div>
      </div>
    </LoadState>
  </div>
</template>

<style scoped>
.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 24px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fdfdff 0%, #f4f6ff 100%);
}

.avatar {
  width: 84px;
  height: 84px;
  flex-shrink: 0;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: var(--shadow-card);
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.profile-info {
  min-width: 0;
}

.nickname {
  font-size: 20px;
  font-weight: 700;
}

.intro {
  margin-top: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.email {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--color-primary);
}

.email:hover {
  text-decoration: underline;
}

.content-card {
  padding: 24px;
}

@media (max-width: 560px) {
  .profile-card {
    flex-direction: column;
    text-align: center;
    align-items: center;
  }
}
</style>
