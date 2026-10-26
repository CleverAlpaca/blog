<template>
  <div class="page-container article-edit" v-loading="pageLoading">
    <!-- 左列：Markdown 编辑器 -->
    <div class="edit-main">
      <MdEditor
        v-model="form.content"
        class="editor"
        placeholder="开始写作吧…支持 Markdown 语法，图片粘贴/上传自动托管"
        :style="{ height: editorHeight }"
        @on-upload-img="handleUploadImg"
        @on-save="handleSave"
      />
    </div>

    <!-- 右列：文章信息表单 -->
    <el-card shadow="never" class="edit-side">
      <template #header>
        <span class="side-title">{{ isEdit ? '编辑文章' : '写文章' }}</span>
      </template>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="标题" required>
          <el-input v-model.trim="form.title" placeholder="文章标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类（可空）" clearable style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="form.tagIds" placeholder="选择标签（可多选）" multiple style="width: 100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button :value="0">草稿</el-radio-button>
            <el-radio-button :value="1">已发布</el-radio-button>
            <el-radio-button :value="2">已下架</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" active-text="置顶" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="留空则自动截取正文前 100 字"
          />
        </el-form-item>

        <el-form-item label="封面图">
          <ImageUpload v-model="form.coverImage" height="140px" />
        </el-form-item>
      </el-form>

      <div class="side-actions">
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="router.push('/articles')">返回列表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import ImageUpload from '@/components/ImageUpload.vue'
import { getArticle, createArticle, updateArticle } from '@/api/article'
import { listCategories } from '@/api/category'
import { listTags } from '@/api/tag'
import { uploadImage, validateImage } from '@/api/upload'
import type { ArticleSaveParams, CategoryItem, TagItem } from '@/api/types'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(false)
const saving = ref(false)
const categories = ref<CategoryItem[]>([])
const tags = ref<TagItem[]>([])

const form = reactive({
  title: '',
  summary: '',
  coverImage: '',
  categoryId: undefined as number | undefined,
  tagIds: [] as number[],
  content: '',
  status: 0,
  isTop: false
})

const articleId = computed(() => (route.query.id ? Number(route.query.id) : null))
const isEdit = computed(() => articleId.value != null)

const editorHeight = computed(() => 'calc(100vh - 120px)')

function resetForm() {
  form.title = ''
  form.summary = ''
  form.coverImage = ''
  form.categoryId = undefined
  form.tagIds = []
  form.content = ''
  form.status = 0
  form.isTop = false
}

/** 编辑回显 */
async function loadArticle() {
  resetForm()
  if (articleId.value == null || Number.isNaN(articleId.value)) return
  pageLoading.value = true
  try {
    const detail = await getArticle(articleId.value)
    form.title = detail.title
    form.summary = detail.summary || ''
    form.coverImage = detail.coverImage || ''
    form.categoryId = detail.categoryId ?? undefined
    form.tagIds = (detail.tags || []).map((t) => t.id)
    form.content = detail.content || ''
    form.status = detail.status ?? 0
    form.isTop = detail.isTop === 1
  } catch {
    // 拦截器已统一提示
  } finally {
    pageLoading.value = false
  }
}

// 「编辑文章 A」→「写文章」等路由复用场景，监听 id 变化重新加载
watch(
  () => route.query.id,
  () => loadArticle(),
  { immediate: true }
)

/** 编辑器图片上传：对接 /api/admin/image/upload */
async function handleUploadImg(files: File[], callback: (urls: string[]) => void) {
  const urls: string[] = []
  for (const file of files) {
    if (!validateImage(file)) {
      ElMessage.error(`「${file.name}」仅支持 jpg/png/gif/webp 格式，且大小不超过 5MB`)
      continue
    }
    try {
      urls.push(await uploadImage(file))
    } catch {
      // 拦截器已统一提示，跳过该图
    }
  }
  if (urls.length) {
    callback(urls)
  }
}

/** 保存（区分 POST / PUT） */
async function handleSave() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  const payload: ArticleSaveParams = {
    title: form.title.trim(),
    summary: form.summary.trim() || undefined,
    coverImage: form.coverImage || undefined,
    categoryId: form.categoryId,
    tagIds: form.tagIds,
    content: form.content,
    status: form.status,
    isTop: form.isTop ? 1 : 0
  }

  saving.value = true
  try {
    if (articleId.value != null) {
      await updateArticle(articleId.value, payload)
      ElMessage.success('保存成功')
    } else {
      await createArticle(payload)
      ElMessage.success('创建成功')
    }
    router.push('/articles')
  } catch {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [categoryList, tagList] = await Promise.all([listCategories(), listTags()])
    categories.value = categoryList
    tags.value = tagList
  } catch {
    // 拦截器已统一提示
  }
})
</script>

<style scoped>
.article-edit {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.edit-main {
  flex: 1;
  min-width: 0;
}

.editor {
  border-radius: 6px;
  overflow: hidden;
}

.edit-side {
  width: 380px;
  flex-shrink: 0;
}

.side-title {
  font-weight: 600;
}

.side-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
</style>
