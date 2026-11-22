import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant, { showToast } from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import { setHttpHandlers } from './api/http'
import { useUserStore } from './stores/user'
import { permit } from './directives/permit'
import './styles/index.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(Vant)
app.directive('permit', permit)
app.config.errorHandler = () => {
  showToast('页面出现异常，请刷新后重试')
}

setHttpHandlers({
  onError(message) {
    showToast(message)
  },
  onUnauthorized() {
    useUserStore().clearSession()
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
  }
})

app.mount('#app')
