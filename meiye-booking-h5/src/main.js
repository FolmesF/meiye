import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import { setHttpHandlers } from './api/http'
import { useUserStore } from './stores/user'
import { toast } from './utils/feedback'
import { permit } from './directives/permit'
import './utils/rem'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(Vant)
app.directive('permit', permit)
app.config.errorHandler = () => {
  toast('页面出现异常，请刷新后重试')
}

setHttpHandlers({
  onError(message) {
    toast(message)
  },
  onUnauthorized() {
    useUserStore().clearSession()
    if (router.currentRoute.value.path !== '/login') {
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    }
  }
})

app.mount('#app')
