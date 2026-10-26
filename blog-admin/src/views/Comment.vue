<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 筛选栏 -->
      <div class="table-toolbar">
        <el-input
          v-model.trim="query.articleTitle"
          placeholder="文章标题"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-input
          v-model.trim="query.nickname"
          placeholder="评论昵称"
          clearable
          style="width: 180px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无评论">
        <el-table-column label="文章标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.articleTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
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
        <el-table-column label="时间" width="170">
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
import { pageComments, updateCommentStatus, deleteComment } from '@/api/comment'
import { formatDateTime } from '@/utils/format'
import type { AdminComment, CommentPageParams } from '@/api/types'

const loading = ref(false)
const list = ref<AdminComment[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  articleTitle: '',
  nickname: ''
})

function buildParams(): CommentPageParams {
  const params: CommentPageParams = {
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.articleTitle) params.articleTitle = query.articleTitle
  if (query.nickname) params.nickname = query.nickname
  return params
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pageComments(buildParams())
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
  query.articleTitle = ''
  query.nickname = ''
  query.pageNum = 1
  fetchList()
}

/** 显示 / 隐藏 */
async function handleStatusChange(row: AdminComment) {
  try {
    await updateCommentStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已显示' : '已隐藏')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function handleDelete(row: AdminComment) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.nickname}」的这条评论吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteComment(row.id)
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
