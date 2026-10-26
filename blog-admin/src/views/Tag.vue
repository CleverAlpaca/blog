<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增标签</el-button>
      </div>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无标签">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="标签名称" min-width="200">
          <template #default="{ row }">
            <el-tag size="small">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="关联文章数" width="120" align="center">
          <template #default="{ row }">
            {{ row.count ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '新增标签'" width="420px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="标签名称" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listTags, createTag, updateTag, deleteTag } from '@/api/tag'
import type { TagItem } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const list = ref<TagItem[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: 0,
  name: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await listTags()
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function openDialog(row?: TagItem) {
  form.id = row?.id ?? 0
  form.name = row?.name ?? ''
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (form.id) {
      await updateTag(form.id, { name: form.name })
      ElMessage.success('修改成功')
    } else {
      await createTag({ name: form.name })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 拦截器已统一提示（重名等）
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: TagItem) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteTag(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(fetchList)
</script>
