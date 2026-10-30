<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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
  }>(),
  { replyTo: '', compact: false, submitting: false, placeholder: '' }
)

const emit = defineEmits<{
  submit: [body: CommentPost]
  cancel: []
}>()

const PROFILE_KEY = 'commenter_profile'

const form = reactive<{ nickname: string; email: string; content: string }>({ nickname: '', email: '', content: '' })
const validated = ref(false)

onMounted(() => {
  try {
    const raw = localStorage.getItem(PROFILE_KEY)
    if (raw) {
      const profile = JSON.parse(raw) as { nickname?: string; email?: string }
      form.nickname = profile.nickname || ''
      form.email = profile.email || ''
    }
  } catch {
    /* 忽略损坏的历史数据 */
  }
})

function handleSubmit(): void {
  validated.value = true
  const nickname = form.nickname.trim()
  const content = form.content.trim()
  if (!nickname) return
  if (!content) return
  const body: CommentPost = { nickname, content }
  const email = form.email.trim()
  if (email) body.email = email
  emit('submit', body)
}

/** 供父组件在提交成功后调用：清空内容并记忆身份 */
defineExpose({
  reset(): void {
    validated.value = false
    form.content = ''
    try {
      localStorage.setItem(PROFILE_KEY, JSON.stringify({ nickname: form.nickname.trim(), email: form.email.trim() }))
    } catch {
      /* localStorage 不可用时忽略 */
    }
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
      <div class="row">
        <input v-model="form.nickname" class="form-input" type="text" maxlength="32" placeholder="昵称（必填）" />
        <input v-model="form.email" class="form-input" type="email" maxlength="128" placeholder="邮箱（选填，不会公开）" />
      </div>
      <textarea
        v-model="form.content"
        class="form-textarea"
        :class="{ invalid: validated && !form.content.trim() }"
        maxlength="1000"
        :placeholder="props.placeholder || '说点什么吧…（支持 1000 字以内）'"
      ></textarea>
      <div class="actions">
        <span v-if="validated && !form.nickname.trim()" class="field-error">请填写昵称</span>
        <span class="count">{{ form.content.length }}/1000</span>
        <button class="btn btn-primary" type="submit" :disabled="props.submitting">
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

.form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

@media (max-width: 560px) {
  .row {
    grid-template-columns: 1fr;
  }
}

.form-textarea.invalid {
  border-color: var(--color-danger);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-error {
  color: var(--color-danger);
  font-size: 12.5px;
}

.count {
  flex: 1;
  text-align: right;
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
