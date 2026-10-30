import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { config } from 'md-editor-v3'
import screenfull from 'screenfull'
import katex from 'katex'
import hljs from 'highlight.js/lib/common'
import Cropper from 'cropperjs'
import App from './App.vue'
import router from './router'
// 先引入 md-editor-v3 预览基础样式，再引入自有样式以便覆盖
import 'md-editor-v3/lib/preview.css'
import 'katex/dist/katex.min.css'
import './styles/main.css'
import './styles/markdown.css'

// md-editor 扩展本地化：代码高亮 / 公式等改用 npm 依赖，不再运行时从 unpkg CDN 加载
config({
  editorExtensions: {
    highlight: { instance: hljs },
    katex: { instance: katex },
    screenfull: { instance: screenfull },
    cropper: { instance: Cropper }
  }
})

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
