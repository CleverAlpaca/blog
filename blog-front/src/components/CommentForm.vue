<script setup lang="ts">
import { reactive } from 'vue'
import type { CommentPost } from '@/types/api'
import AppIcon from './AppIcon.vue'

const props = withDefaults(
  defineProps<{
    /** 回复对象昵称（显示"回复 @xxx"），为空表示顶级发表 */
    replyTo?: string
    /** 楼内内联表单（更紧凑） */
    compact?: boolean
    submitting?: boolean
    placeholder?: string
    /** 发表者昵称（登录态展示，不可改） */
    nickname?: string
  }>(),
  { replyTo: '', compact: false, submitting: false, placeholder: '', nickname: '' }
)

const emit = defineEmits<{
  submit: [body: CommentPost]
  cancel: []
}>()

const form = reactive({ content: '' })

function handleSubmit(): void {
  const content = form.content.trim()
  if (!content) return
  emit('submit', { content })
}

/** 供父组件在提交成功后调用：清空内容 */
defineExpose({
  reset(): void {
    form.content = ''
  }
})
</script>

<template>
  <div class="comment-form" :class="{ compact: props.compact }">
    <div v-if="props.replyTo" class="reply-banner">
      <AppIcon name="corner-up-left" :size="13" />
      回复 @{{ props.replyTo }}
      <button type="button" class="cancel-reply" aria-label="取消回复" @click="emit('cancel')">
        <AppIcon name="x" :size="13" />
      </button>
    </div>

    <form class="form-body" @submit.prevent="handleSubmit">
      <div v-if="props.nickname" class="identity">
        <AppIcon name="user" :size="13" />
        以 <strong>{{ props.nickname }}</strong> 的身份发表
      </div>
      <textarea
        v-model="form.content"
        class="form-textarea"
        maxlength="1000"
        :placeholder="props.placeholder || '说点什么吧…（支持 1000 字以内）'"
      ></textarea>
      <div class="actions">
        <span class="count">{{ form.content.length }}/1000</span>
        <button class="btn btn-primary" type="submit" :disabled="props.submitting || !form.content.trim()">
          <AppIcon name="send" :size="14" />
          {{ props.submitting ? '发布中…' : '发表' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.comment-form {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-small);
  padding: 14px;
  background: var(--form-soft);
}

.comment-form.compact {
  padding: 10px;
  background: var(--color-blockquote-bg);
}

.reply-banner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 13px;
}

.cancel-reply {
  display: inline-flex;
  align-items: center;
  color: inherit;
  opacity: 0.7;
}

.cancel-reply:hover {
  opacity: 1;
}

.identity {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.identity strong {
  color: var(--color-text-secondary);
  font-weight: 600;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-textarea {
  resize: vertical;
  min-height: 88px;
  line-height: 1.6;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.count {
  flex: 1;
  text-align: right;
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
