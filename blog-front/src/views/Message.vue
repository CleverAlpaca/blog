<script setup lang="ts">
import type { CommentNode, CommentPost } from '@/types/api'
import { getMessageTree, postMessage } from '@/api/message'
import { usePageTitle } from '@/composables/usePageTitle'
import CommentSection from '@/components/CommentSection.vue'
import AppIcon from '@/components/AppIcon.vue'

usePageTitle('留言板')

async function submit(body: CommentPost): Promise<void> {
  await postMessage(body)
}
</script>

<template>
  <div class="message-page">
    <header class="page-header">
      <h1><AppIcon name="message-circle" :size="20" /> 留言板</h1>
      <p class="page-desc">有什么想对博主说的，留下一句话吧（昵称必填，邮箱选填）</p>
    </header>

    <CommentSection
      :fetch-list="getMessageTree"
      :submit="submit"
      title="留言"
      placeholder="写下你的留言…（支持 1000 字以内）"
      empty-text="还没有留言，来当第一个留言的人吧"
    />
  </div>
</template>
