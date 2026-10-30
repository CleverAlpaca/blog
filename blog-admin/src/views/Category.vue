<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无分类">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="100" align="center" />
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="分类描述（选填）"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
          <span class="sort-tip">越小越靠前</span>
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
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { Plus } from '@element-plus/icons-vue'
import { listCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import type { CategoryItem } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const list = ref<CategoryItem[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: 0,
  name: '',
  description: '',
  sort: 0
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await listCategories()
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function openDialog(row?: CategoryItem) {
  form.id = row?.id ?? 0
  form.name = row?.name ?? ''
  form.description = row?.description ?? ''
  form.sort = row?.sort ?? 0
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description || undefined,
      sort: form.sort
    }
    if (form.id) {
      await updateCategory(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createCategory(payload)
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

async function handleDelete(row: CategoryItem) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(fetchList)
</script>

<style scoped>
.sort-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
