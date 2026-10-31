<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createMyArticle, getMyArticle, updateMyArticle, uploadMyImage } from '@/api/my'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import type { Category, Tag } from '@/types/api'
import { usePageTitle } from '@/composables/usePageTitle'
import { useTheme } from '@/composables/useTheme'
import { showError, showSuccess } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const { theme } = useTheme()
const editId = computed(() => (route.query.id ? Number(route.query.id) : null))
usePageTitle(() => (editId.value ? '编辑投稿' : '写文章'))

const form = reactive({
  title: '',
  summary: '',
  coverImage: '',
  categoryId: undefined as number | undefined,
  tagIds: [] as number[],
  content: ''
})
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const rejectReason = ref('')
const loading = ref(false)
const saving = ref(false)

async function loadOptions(): Promise<void> {
  try {
    const [cats, tgs] = await Promise.all([getCategoryList(), getTagList()])
    categories.value = cats
    tags.value = tgs
  } catch {
    /* 拦截器已提示 */
  }
}

async function loadArticle(): Promise<void> {
  if (!editId.value) return
  loading.value = true
  try {
    const detail = await getMyArticle(editId.value)
    form.title = detail.title
    form.summary = detail.summary || ''
    form.coverImage = detail.coverImage || ''
    form.categoryId = detail.categoryId ?? undefined
    form.tagIds = (detail.tags ?? []).map((t) => t.id)
    form.content = detail.content || ''
    rejectReason.value = detail.rejectReason || ''
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

async function onUploadImg(files: File[], callback: (urls: string[]) => void): Promise<void> {
  const urls: string[] = []
  for (const file of files) {
    try {
      const { url } = await uploadMyImage(file)
      urls.push(url)
    } catch {
      /* 拦截器已提示 */
    }
  }
  callback(urls)
}

function toggleTag(id: number): void {
  const idx = form.tagIds.indexOf(id)
  if (idx > -1) form.tagIds.splice(idx, 1)
  else form.tagIds.push(id)
}

function pickCover(event: Event): void {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploadMyImage(file)
    .then(({ url }) => {
      form.coverImage = url
    })
    .catch(() => undefined)
    .finally(() => {
      input.value = ''
    })
}

async function save(action: 'draft' | 'submit'): Promise<void> {
  if (!form.title.trim()) {
    showError('请填写标题')
    return
  }
  if (!form.content.trim()) {
    showError('请填写正文')
    return
  }
  saving.value = true
  try {
    const body = {
      title: form.title.trim(),
      summary: form.summary.trim() || undefined,
      coverImage: form.coverImage || undefined,
      categoryId: form.categoryId,
      tagIds: [...form.tagIds],
      content: form.content,
      action
    }
    if (editId.value) {
      await updateMyArticle(editId.value, body)
    } else {
      await createMyArticle(body)
    }
    showSuccess(action === 'submit' ? '已提交审核' : '草稿已保存')
    router.push('/my/articles')
  } catch {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void loadOptions()
  void loadArticle()
})
</script>

<template>
  <div class="write-page">
    <div v-if="rejectReason" class="card reject-banner">
      <strong>退回原因：</strong>{{ rejectReason }}（修改后可重新提交审核）
    </div>

    <div class="write-layout">
      <div class="card editor-card">
        <MdEditor
          v-model="form.content"
          :theme="theme"
          preview-theme="github"
          code-theme="github"
          placeholder="开始写作…（Markdown）"
          @on-upload-img="onUploadImg"
        />
      </div>

      <div class="side-panel">
        <div class="card side-card">
          <div class="side-field">
            <label class="side-label">标题（必填）</label>
            <input v-model="form.title" class="form-input" type="text" maxlength="100" placeholder="文章标题" />
          </div>
          <div class="side-field">
            <label class="side-label">分类（选填）</label>
            <select v-model="form.categoryId" class="form-input">
              <option :value="undefined">不选择</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="side-field">
            <label class="side-label">标签（可多选）</label>
            <div class="tag-picker">
              <button
                v-for="t in tags"
                :key="t.id"
                type="button"
                class="tag-option"
                :class="{ picked: form.tagIds.includes(t.id) }"
                @click="toggleTag(t.id)"
              >
                {{ t.name }}
              </button>
            </div>
          </div>
          <div class="side-field">
            <label class="side-label">摘要（选填，留空自动截取）</label>
            <textarea v-model="form.summary" class="form-input summary-input" rows="3" maxlength="500"></textarea>
          </div>
          <div class="side-field">
            <label class="side-label">封面图（选填）</label>
            <input class="form-input" type="file" accept="image/jpeg,image/png,image/gif,image/webp" @change="pickCover" />
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" alt="封面预览" />
          </div>
        </div>

        <div class="card side-card actions">
          <button class="btn btn-ghost" type="button" :disabled="saving" @click="save('draft')">
            {{ saving ? '处理中…' : '存草稿' }}
          </button>
          <button class="btn btn-primary" type="button" :disabled="saving" @click="save('submit')">
            {{ saving ? '处理中…' : editId ? '重新提交审核' : '提交审核' }}
          </button>
        </div>
        <p class="submit-note">投稿提交后进入待审核状态，管理员审核通过即发布。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.write-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.editor-card {
  padding: 0;
  overflow: hidden;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: calc(var(--header-height) + 16px);
}

.side-card {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.side-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.tag-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-option {
  padding: 4px 12px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
}

.tag-option.picked {
  background: var(--color-primary-soft);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.cover-preview {
  margin-top: 8px;
  width: 100%;
  border-radius: var(--radius-small);
}

.actions {
  flex-direction: row;
  gap: 10px;
}

.actions .btn {
  flex: 1;
}

.submit-note {
  font-size: 12.5px;
  color: var(--color-text-tertiary);
}

.reject-banner {
  padding: 12px 16px;
  margin-bottom: 16px;
  border: 1px solid var(--color-warning);
  color: var(--color-text);
  font-size: 14px;
}

@media (max-width: 900px) {
  .write-layout {
    grid-template-columns: 1fr;
  }

  .side-panel {
    position: static;
  }
}
</style>
