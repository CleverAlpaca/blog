import { ref } from 'vue'

export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'theme'

function initTheme(): Theme {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved === 'dark' || saved === 'light') return saved
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

// 模块级单例：全站共享同一主题状态（index.html 已在首屏前做过同样的初始化）
const theme = ref<Theme>(initTheme())
apply(theme.value)

function apply(value: Theme): void {
  theme.value = value
  document.documentElement.dataset.theme = value
  localStorage.setItem(STORAGE_KEY, value)
}

export function useTheme() {
  return {
    theme,
    isDark: () => theme.value === 'dark',
    toggleTheme: () => apply(theme.value === 'dark' ? 'light' : 'dark')
  }
}
