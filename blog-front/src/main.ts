import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 先引入 md-editor-v3 预览基础样式，再引入自有样式以便覆盖
import 'md-editor-v3/lib/preview.css'
import './styles/main.css'
import './styles/markdown.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
