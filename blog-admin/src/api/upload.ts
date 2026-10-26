import request from '@/utils/request'

/**
 * 上传图片（multipart 字段名 file）
 * 后端仅接受 jpg/jpeg/png/gif/webp，且 ≤ 5MB
 */
export async function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const data = await request.post<{ url: string }>('/admin/image/upload', formData)
  return data.url
}

/** 上传前校验：类型与大小 */
export function validateImage(file: File): boolean {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  const allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'webp']
  const ext = (file.name.split('.').pop() || '').toLowerCase()
  if (!allowedTypes.includes(file.type) && !allowedExts.includes(ext)) {
    return false
  }
  return file.size / 1024 / 1024 <= 5
}
