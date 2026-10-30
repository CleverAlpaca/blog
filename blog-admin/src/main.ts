import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { vLoading as ElLoadingDirective } from 'element-plus/es/components/loading/index.mjs'
import { config } from 'md-editor-v3'
import screenfull from 'screenfull'
import katex from 'katex'
import hljs from 'highlight.js/lib/common'
import Cropper from 'cropperjs'
import 'katex/dist/katex.min.css'

// md-editor 扩展本地化：编辑器扩展改用 npm 依赖，不再运行时从 unpkg CDN 加载
config({
  editorExtensions: {
    highlight: { instance: hljs },
    katex: { instance: katex },
    screenfull: { instance: screenfull },
    cropper: { instance: Cropper }
  }
})

// 按需引入下，命令式 API（ElMessage/ElMessageBox）与 v-loading 指令的样式/注册需手动处理
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/notification/style/css'
import 'element-plus/es/components/loading/style/css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import '@/styles/index.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
// v-loading 指令（按需引入需手动注册）
app.directive('loading', ElLoadingDirective)

// 全局注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
