<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="用户名 / 昵称"
            clearable
            style="width: 220px"
            @keyup.enter="handleFilter"
            @clear="handleFilter"
          >
            <template #append>
              <el-button :icon="Search" @click="handleFilter" />
            </template>
          </el-input>
          <el-radio-group v-model="statusFilter" @change="handleFilter">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="1">正常</el-radio-button>
            <el-radio-button :value="0">已封禁</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无用户">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role !== 'ADMIN'">
              <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleStatus(row)">
                {{ row.status === 1 ? '封禁' : '解封' }}
              </el-button>
              <el-button link type="warning" @click="handleReset(row)">重置密码</el-button>
            </template>
            <span v-else class="admin-tip">管理员账号</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { Search } from '@element-plus/icons-vue'
import { getUsers, resetUserPassword, updateUserStatus } from '@/api/userManage'
import type { UserManageVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const list = ref<UserManageVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const page = await getUsers({
      pageNum: pageNum.value,
      pageSize,
      keyword: keyword.value || undefined,
      status: statusFilter.value
    })
    list.value = page.records
    total.value = page.total
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function handleFilter(): void {
  pageNum.value = 1
  void fetchList()
}

async function handleStatus(row: UserManageVO): Promise<void> {
  const banning = row.status === 1
  try {
    await ElMessageBox.confirm(
      banning ? `确定封禁用户「${row.nickname || row.username}」吗？封禁后其会话将立即失效。` : `确定解封用户「${row.nickname || row.username}」吗？`,
      banning ? '封禁确认' : '解封确认',
      { confirmButtonText: banning ? '封禁' : '解封', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateUserStatus(row.id, banning ? 0 : 1)
    ElMessage.success(banning ? '已封禁' : '已解封')
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

async function handleReset(row: UserManageVO): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定重置「${row.nickname || row.username}」的密码吗？将生成 8 位随机密码并使其会话失效。`,
      '重置密码',
      { confirmButtonText: '重置', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    const newPassword = await resetUserPassword(row.id)
    ElMessageBox.alert(`新密码：${newPassword}（仅显示一次，请告知用户）`, '重置成功', {
      confirmButtonText: '我已记录',
      type: 'success'
    })
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(fetchList)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.admin-tip {
  font-size: 12.5px;
  color: #909399;
}
</style>
