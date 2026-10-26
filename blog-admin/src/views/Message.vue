<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 筛选栏（留言无文章标题） -->
      <div class="table-toolbar">
        <el-input
          v-model.trim="query.nickname"
          placeholder="留言昵称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无留言">
        <el-table-column prop="nickname" label="昵称" width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { pageMessages, updateMessageStatus, deleteMessage } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import type { AdminComment, MessagePageParams } from '@/api/types'

const loading = ref(false)
const list = ref<AdminComment[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  nickname: ''
})

function buildParams(): MessagePageParams {
  const params: MessagePageParams = {
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.nickname) params.nickname = query.nickname
  return params
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pageMessages(buildParams())
    list.value = data.records
    total.value = data.total
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleReset() {
  query.nickname = ''
  query.pageNum = 1
  fetchList()
}

/** 显示 / 隐藏 */
async function handleStatusChange(row: AdminComment) {
  try {
    await updateMessageStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已显示' : '已隐藏')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function handleDelete(row: AdminComment) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.nickname}」的这条留言吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteMessage(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1
    }
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(fetchList)
</script>
