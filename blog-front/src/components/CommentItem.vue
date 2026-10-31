<script setup lang="ts">
import type { CommentNode } from '@/types/api'
import { formatDateTime } from '@/utils/format'
import AppIcon from './AppIcon.vue'

defineProps<{ node: CommentNode; nested?: boolean }>()

const emit = defineEmits<{ reply: [node: CommentNode] }>()

/** 头像：昵称首字 + 固定色（按 id 哈希轮换） */
const avatarColors = ['#4f6ef7', '#f59e0b', '#10b981', '#f43f5e', '#8b5cf6', '#0ea5e9']

function avatarStyle(node: CommentNode): { background: string } {
  const index = Math.abs(Number(node.id) || 0) % avatarColors.length
  return { background: avatarColors[index] }
}

function initial(node: CommentNode): string {
  return (node.nickname || '访').charAt(0).toUpperCase()
}
</script>

<template>
  <div class="comment-item" :class="{ nested }">
    <div class="head">
      <div class="mini-avatar" :style="avatarStyle(node)">{{ initial(node) }}</div>
      <RouterLink v-if="node.userId" :to="`/user/${node.userId}`" class="nickname linked">
        {{ node.nickname }}
      </RouterLink>
      <span v-else class="nickname">{{ node.nickname }}</span>
      <span class="time">{{ formatDateTime(node.createTime) }}</span>
      <button type="button" class="reply-btn" @click="emit('reply', node)">回复</button>
    </div>
    <p class="content">{{ node.content }}</p>

    <div v-if="node.replies && node.replies.length" class="replies">
      <CommentItem v-for="reply in node.replies" :key="reply.id" :node="reply" nested @reply="emit('reply', $event)" />
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--color-border);
}

.comment-item:last-child {
  border-bottom: none;
}

.head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.nickname {
  font-size: 14px;
  font-weight: 600;
}

a.nickname:hover {
  color: var(--color-primary);
}

.time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.reply-btn {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
  padding: 2px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.reply-btn:hover {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.content {
  margin-top: 8px;
  padding-left: 40px;
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
}

/* 楼内回复 */
.replies {
  margin: 4px 0 0 40px;
  padding-left: 12px;
  border-left: 2px solid var(--color-border);
}

.replies .head {
  gap: 6px;
}

.replies .mini-avatar {
  width: 26px;
  height: 26px;
  font-size: 12px;
}

.replies .content {
  margin-top: 6px;
  padding-left: 0;
  font-size: 13.5px;
}

@media (max-width: 560px) {
  .replies {
    margin-left: 12px;
  }
}
</style>
