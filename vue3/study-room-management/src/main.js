import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './styles/responsive.css'
import './styles/theme.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 自定义Element Plus主题变量
document.documentElement.style.setProperty('--el-color-primary', '#1890ff')
document.documentElement.style.setProperty('--el-color-success', '#52c41a')
document.documentElement.style.setProperty('--el-color-warning', '#faad14')
document.documentElement.style.setProperty('--el-color-danger', '#ff4d4f')
document.documentElement.style.setProperty('--el-color-info', '#13c2c2')

app.use(ElementPlus)
app.use(createPinia())
app.use(router)
app.mount('#app')