<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <span class="card-title">站点配置</span>
      </template>

      <el-form :model="form" label-width="100px" style="max-width: 680px">
        <el-form-item label="站点名称">
          <el-input v-model.trim="form.site_name" maxlength="50" show-word-limit placeholder="前台 Logo / 页脚 / 浏览器标题后缀 / RSS 频道名" />
        </el-form-item>
        <el-form-item label="站点描述">
          <el-input
            v-model="form.site_description"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="首页 SEO description 与 RSS 频道描述"
          />
        </el-form-item>
        <el-form-item label="公告">
          <el-input
            v-model="form.announcement"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="展示在前台侧栏，留空则隐藏"
          />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model.trim="form.icp" maxlength="100" placeholder="展示在前台页脚，留空则隐藏" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
          <span class="save-tip">保存后前台刷新即可生效</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { getSiteConfig, updateSiteConfig, type SiteConfigMap } from '@/api/siteConfig'

const loading = ref(false)
const saving = ref(false)
// 全量键值对（保留后台未在表单中出现的键），表单只绑定以下四个
const raw = ref<SiteConfigMap>({})
const form = reactive({
  site_name: '',
  site_description: '',
  announcement: '',
  icp: ''
})

onMounted(async () => {
  loading.value = true
  try {
    raw.value = await getSiteConfig()
    form.site_name = raw.value.site_name ?? ''
    form.site_description = raw.value.site_description ?? ''
    form.announcement = raw.value.announcement ?? ''
    form.icp = raw.value.icp ?? ''
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
})

async function handleSave(): Promise<void> {
  if (!form.site_name.trim()) {
    ElMessage.warning('站点名称不能为空')
    return
  }
  saving.value = true
  try {
    await updateSiteConfig({
      ...raw.value,
      site_name: form.site_name,
      site_description: form.site_description,
      announcement: form.announcement,
      icp: form.icp
    })
    ElMessage.success('保存成功，前台刷新即可生效')
  } catch {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.card-title {
  font-weight: 600;
}

.save-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>
