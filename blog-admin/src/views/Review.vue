<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="toolbar">
          <el-radio-group v-model="statusFilter" @change="handleFilter">
            <el-radio-button :value="undefined">全部</el-radio-button>
            <el-radio-button :value="3">待审核</el-radio-button>
            <el-radio-button :value="4">已退回</el-radio-button>
          </el-radio-group>
          <el-input
            v-model="keyword"
            placeholder="按标题搜索"
            clearable
            style="width: 220px"
            @keyup.enter="handleFilter"
            @clear="handleFilter"
          >
            <template #append>
              <el-button :icon="Search" @click="handleFilter" />
            </template>
          </el-input>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无待审核投稿">
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 3 ? 'warning' : 'info'" size="small">
              {{ row.status === 3 ? '待审核' : '已退回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退回原因" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.rejectReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 3">
              <el-button link type="primary" @click="handleApprove(row)">通过</el-button>
              <el-button link type="warning" @click="openReject(row)">退回</el-button>
            </template>
            <span v-else class="returned-tip">已退回</span>
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

    <el-dialog v-model="rejectVisible" title="退回投稿" width="460px" destroy-on-close>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="请填写退回原因（作者可见），如：内容与博客定位不符 / 存在错误请修正后重投"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" :loading="acting" @click="confirmReject">确认退回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { Search } from '@element-plus/icons-vue'
import { approveReview, getReviewPage, rejectReview } from '@/api/review'
import type { AdminReviewVO } from '@/api/types'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const acting = ref(false)
const list = ref<AdminReviewVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const statusFilter = ref<number | undefined>(undefined)
const keyword = ref('')

const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectTarget = ref<AdminReviewVO | null>(null)

async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const page = await getReviewPage({
      pageNum: pageNum.value,
      pageSize,
      status: statusFilter.value,
      keyword: keyword.value || undefined
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

async function handleApprove(row: AdminReviewVO): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定通过投稿「${row.title}」吗？通过后将立即发布。`, '审核通过', {
      confirmButtonText: '通过并发布',
      cancelButtonText: '取消',
      type: 'success'
    })
  } catch {
    return
  }
  acting.value = true
  try {
    await approveReview(row.id)
    ElMessage.success('已通过并发布')
    fetchList()
  } catch {
    // 拦截器已统一提示
  } finally {
    acting.value = false
  }
}

function openReject(row: AdminReviewVO): void {
  rejectTarget.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject(): Promise<void> {
  if (!rejectTarget.value) return
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写退回原因')
    return
  }
  acting.value = true
  try {
    await rejectReview(rejectTarget.value.id, rejectReason.value.trim())
    ElMessage.success('已退回')
    rejectVisible.value = false
    fetchList()
  } catch {
    // 拦截器已统一提示
  } finally {
    acting.value = false
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

.returned-tip {
  font-size: 12.5px;
  color: #909399;
}
</style>
