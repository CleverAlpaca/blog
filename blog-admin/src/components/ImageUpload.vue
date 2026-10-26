<template>
  <div class="image-upload" :style="{ width, height }">
    <el-upload
      v-if="!modelValue"
      accept="image/jpeg,image/png,image/gif,image/webp"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="doUpload"
    >
      <div class="upload-trigger" :style="{ width, height }">
        <el-icon :size="22"><Plus /></el-icon>
        <span>{{ uploading ? '上传中…' : '上传图片' }}</span>
        <span v-if="tip" class="upload-tip">{{ tip }}</span>
      </div>
    </el-upload>

    <div v-else class="preview-box" :style="{ width, height }">
      <img :src="modelValue" class="preview-img" alt="图片" />
      <div class="preview-mask">
        <el-button size="small" type="danger" plain @click="remove">删除</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage, validateImage } from '@/api/upload'

const props = withDefaults(
  defineProps<{
    modelValue: string
    width?: string
    height?: string
    tip?: string
  }>(),
  {
    width: '100%',
    height: '120px',
    tip: 'jpg / png / gif / webp ≤ 5MB'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const uploading = ref(false)

function beforeUpload(file: File) {
  if (!validateImage(file)) {
    ElMessage.error('仅支持 jpg/png/gif/webp 格式，且大小不超过 5MB')
    return false
  }
  return true
}

// el-upload 自定义上传：走后端 /api/admin/image/upload 接口
async function doUpload(options: { file: File; onSuccess?: (res: unknown) => void; onError?: (err: unknown) => void }) {
  uploading.value = true
  try {
    const url = await uploadImage(options.file)
    emit('update:modelValue', url)
    options.onSuccess?.(url)
  } catch (err) {
    options.onError?.(err)
  } finally {
    uploading.value = false
  }
}

function remove() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload :deep(.el-upload) {
  display: block;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px dashed #c0ccda;
  border-radius: 6px;
  background-color: #fafbfc;
  color: #909399;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-tip {
  font-size: 11px;
  color: #c0c4cc;
}

.preview-box {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.35);
  opacity: 0;
  transition: opacity 0.2s;
}

.preview-box:hover .preview-mask {
  opacity: 1;
}
</style>
