<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增友链</el-button>
      </div>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无友链">
        <el-table-column label="站点名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="site-cell">
              <el-avatar :size="28" :src="row.logo || undefined" shape="square">
                <el-icon><Link /></el-icon>
              </el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="URL" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" :href="row.url" target="_blank">{{ row.url }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑友链' : '新增友链'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model.trim="form.name" placeholder="站点名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="Logo" prop="logo">
          <div class="logo-field">
            <ImageUpload v-model="form.logo" width="96px" height="96px" tip="上传 Logo" />
            <el-input
              v-model.trim="form.logo"
              placeholder="或直接填写 Logo 图片 URL"
              clearable
              class="logo-input"
            />
          </div>
        </el-form-item>
        <el-form-item label="URL" prop="url">
          <el-input v-model.trim="form.url" placeholder="https://example.com" maxlength="512" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="站点描述（选填）"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
          <span class="sort-tip">越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="显示" inactive-text="隐藏" />
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
import { Plus, Link } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import { listFriendLinks, createFriendLink, updateFriendLink, deleteFriendLink } from '@/api/friendLink'
import type { FriendLinkItem } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const list = ref<FriendLinkItem[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: 0,
  name: '',
  logo: '',
  url: '',
  description: '',
  sort: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入站点名称', trigger: 'blur' }],
  url: [
    { required: true, message: '请输入站点地址', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value && !/^https?:\/\//i.test(value)) {
          callback(new Error('地址需以 http:// 或 https:// 开头'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await listFriendLinks()
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function openDialog(row?: FriendLinkItem) {
  form.id = row?.id ?? 0
  form.name = row?.name ?? ''
  form.logo = row?.logo ?? ''
  form.url = row?.url ?? ''
  form.description = row?.description ?? ''
  form.sort = row?.sort ?? 0
  form.status = row?.status ?? 1
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
      logo: form.logo || undefined,
      url: form.url,
      description: form.description || undefined,
      sort: form.sort,
      status: form.status
    }
    if (form.id) {
      await updateFriendLink(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createFriendLink(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: FriendLinkItem) {
  try {
    await ElMessageBox.confirm(`确定删除友链「${row.name}」吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteFriendLink(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(fetchList)
</script>

<style scoped>
.site-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-field {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
}

.logo-input {
  flex: 1;
}

.sort-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
