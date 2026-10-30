<template>
  <div class="page-container">
    <el-card shadow="never">
      <!-- 筛选栏 -->
      <div class="table-toolbar">
        <el-input
          v-model.trim="query.keyword"
          placeholder="标题关键词"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="opt in ARTICLE_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select
          v-model="query.categoryId"
          placeholder="分类"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="spacer"></div>
        <el-button type="primary" :icon="Plus" @click="router.push('/articles/edit')">写文章</el-button>
      </div>

      <!-- 列表 -->
      <el-table v-loading="loading" :data="list" style="width: 100%" empty-text="暂无文章">
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small" effect="dark" class="top-tag">顶</el-tag>
            <span>{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="120">
          <template #default="{ row }">
            <template v-if="row.tags && row.tags.length">
              <el-tag v-for="tag in row.tags" :key="tag.id" size="small" class="tag-item" type="info">
                {{ tag.name }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isTop"
              :active-value="1"
              :inactive-value="0"
              @change="handleTopChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览" width="80" align="center" />
        <el-table-column prop="likes" label="点赞" width="80" align="center" />
        <el-table-column prop="commentCount" label="评论" width="80" align="center" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/articles/edit?id=${row.id}`)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { pageArticles, deleteArticle, setArticleTop } from '@/api/article'
import { listCategories } from '@/api/category'
import { ARTICLE_STATUS_OPTIONS, formatDateTime, statusLabel, statusTagType } from '@/utils/format'
import type { ArticleCard, ArticlePageParams, CategoryItem } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const list = ref<ArticleCard[]>([])
const total = ref(0)
const categories = ref<CategoryItem[]>([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '' as number | '',
  categoryId: '' as number | ''
})

function buildParams(): ArticlePageParams {
  const params: ArticlePageParams = {
    pageNum: query.pageNum,
    pageSize: query.pageSize
  }
  if (query.keyword) params.keyword = query.keyword
  if (query.status !== '') params.status = query.status
  if (query.categoryId !== '') params.categoryId = query.categoryId
  return params
}

async function fetchList() {
  loading.value = true
  try {
    const data = await pageArticles(buildParams())
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
  query.keyword = ''
  query.status = ''
  query.categoryId = ''
  query.pageNum = 1
  fetchList()
}

/** 置顶开关 */
async function handleTopChange(row: ArticleCard) {
  try {
    await setArticleTop(row.id, row.isTop)
    ElMessage.success(row.isTop === 1 ? '已置顶' : '已取消置顶')
  } catch {
    // 失败回滚开关状态
    row.isTop = row.isTop === 1 ? 0 : 1
  }
}

/** 删除（二次确认） */
async function handleDelete(row: ArticleCard) {
  try {
    await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？删除后前台将不可见。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1
    }
    fetchList()
  } catch {
    // 拦截器已统一提示
  }
}

onMounted(async () => {
  fetchList()
  try {
    categories.value = await listCategories()
  } catch {
    // 拦截器已统一提示
  }
})
</script>

<style scoped>
.top-tag {
  margin-right: 6px;
}

.tag-item {
  margin-right: 4px;
  margin-bottom: 2px;
}
</style>
