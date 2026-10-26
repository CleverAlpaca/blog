<script setup lang="ts">
import { computed, ref, useTemplateRef } from 'vue'
import type { CommentNode, CommentPost } from '@/types/api'
import { useRequest } from '@/composables/useRequest'
import { showError, showSuccess } from '@/utils/toast'
import LoadState from './LoadState.vue'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'

/**
 * 评论区通用组件（文章评论 / 留言板共用）：
 * 两级树展示 + 顶级发表表单 + 楼内内联回复表单。
 */
const props = withDefaults(
  defineProps<{
    fetchList: () => Promise<CommentNode[]>
    submit: (body: CommentPost) => Promise<void>
    title?: string
    placeholder?: string
    emptyText?: string
    /** 标题是否带数量（评论数） */
    showCount?: boolean
  }>(),
  { title: '评论', placeholder: '', emptyText: '还没有评论，快来抢沙发吧', showCount: true }
)

const { data: comments, loading, error, run } = useRequest<CommentNode[]>(props.fetchList)

const submitting = ref(false)
/** 当前正在回复的顶级评论：null 表示发表顶级评论 */
const replyTarget = ref<CommentNode | null>(null)
const mainFormRef = useTemplateRef<InstanceType<typeof CommentForm>>('mainForm')
const inlineFormRef = useTemplateRef<InstanceType<typeof CommentForm>>('inlineForm')

const totalCount = computed<number>(() => {
  let count = 0
  for (const node of comments.value ?? []) {
    count += 1 + (node.replies?.length ?? 0)
  }
  return count
})

function startReply(node: CommentNode): void {
  // 后端仅两级：楼内回复一律挂到顶级评论下
  replyTarget.value = node.parentId ? null : node
  // 若点击的是楼内回复，需要挂到其顶级评论：由父级链向上找顶级 id
  if (node.parentId) {
    const top = (comments.value ?? []).find((c) => c.id === node.parentId)
    replyTarget.value = top ?? null
  }
}

function cancelReply(): void {
  replyTarget.value = null
}

async function handleSubmit(body: CommentPost, parentId?: number): Promise<void> {
  submitting.value = true
  try {
    await props.submit(parentId ? { ...body, parentId } : body)
    showSuccess(replyTarget.value ? '回复成功' : '发布成功')
    if (replyTarget.value) {
      inlineFormRef.value?.reset()
      cancelReply()
    } else {
      mainFormRef.value?.reset()
    }
    await run()
  } catch (e) {
    // 拦截器已 toast，此处区分限频等业务错误（message 已展示）
    console.error('[comment] submit failed', e)
  } finally {
    submitting.value = false
  }
}

function handleTopSubmit(body: CommentPost): void {
  if (!body.nickname.trim()) {
    showError('请填写昵称')
    return
  }
  void handleSubmit(body)
}

function handleInlineSubmit(body: CommentPost): void {
  const parentId = replyTarget.value?.id
  if (!parentId) {
    cancelReply()
    return
  }
  if (!body.nickname.trim()) {
    showError('请填写昵称')
    return
  }
  void handleSubmit(body, parentId)
}
</script>

<template>
  <section class="comment-section card">
    <h3 class="card-title">
      {{ props.title }}
      <span v-if="props.showCount && totalCount" class="count-badge">{{ totalCount }}</span>
    </h3>

    <!-- 顶级发表表单 -->
    <CommentForm
      ref="mainForm"
      :submitting="submitting"
      :placeholder="props.placeholder"
      @submit="handleTopSubmit"
    />

    <p class="list-total">全部{{ props.title }}（{{ totalCount }}）</p>

    <LoadState
      :loading="loading"
      :error="error"
      :empty="totalCount === 0"
      :empty-text="props.emptyText"
      empty-icon="message-circle"
      @retry="run"
    >
      <div class="comment-list">
        <template v-for="node in comments ?? []" :key="node.id">
          <CommentItem :node="node" @reply="startReply" />

          <!-- 楼内回复：内联展开表单 -->
          <div v-if="replyTarget && replyTarget.id === node.id" class="inline-form">
            <CommentForm
              ref="inlineForm"
              compact
              :reply-to="replyTarget.nickname"
              :submitting="submitting"
              placeholder="写下你的回复…"
              @submit="handleInlineSubmit"
              @cancel="cancelReply"
            />
          </div>
        </template>
      </div>
    </LoadState>
  </section>
</template>

<style scoped>
.comment-section {
  padding: 20px;
  margin-top: 20px;
}

.count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 20px;
  padding: 0 7px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
}

.list-total {
  margin: 18px 0 4px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}

.inline-form {
  margin: 4px 0 14px 40px;
}

@media (max-width: 560px) {
  .inline-form {
    margin-left: 12px;
  }
}
</style>
