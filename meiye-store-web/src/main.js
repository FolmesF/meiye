import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { ElMessage } from 'element-plus'
import { setHttpHandlers } from './api/http'
import { permit } from './directives/permit'
import { useUserStore } from './stores/user'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.directive('permit', permit)
app.config.errorHandler = () => {
  ElMessage.error('页面出现异常，请刷新后重试')
}

setHttpHandlers({
  onError(message) {
    ElMessage.error(message)
  },
  onUnauthorized() {
    useUserStore().clearSession()
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
  }
})

const user = useUserStore()
if (user.token) {
  user.refreshMenus().catch(() => {})
}

app.mount('#app')
