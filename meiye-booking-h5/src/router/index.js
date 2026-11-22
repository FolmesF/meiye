import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import TabLayout from '../layouts/TabLayout.vue'
import LoginView from '../views/login/LoginView.vue'
import HomeView from '../views/home/HomeView.vue'
import BeauticianListView from '../views/booking/BeauticianListView.vue'
import BeauticianDetailView from '../views/booking/BeauticianDetailView.vue'
import OrderListView from '../views/booking/OrderListView.vue'
import MineView from '../views/mine/MineView.vue'
import ProfileView from '../views/mine/ProfileView.vue'
import NotFoundView from '../views/error/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true, title: '登录' } },
    { path: '/beauticians/:id', component: BeauticianDetailView, meta: { public: true, title: '预约详情' } },
    { path: '/profile', component: ProfileView, meta: { title: '账户信息' } },
    { path: '/messages', redirect: '/home' },
    {
      path: '/',
      component: TabLayout,
      redirect: '/home',
      children: [
        { path: 'home', component: HomeView, meta: { title: '优惠活动' } },
        { path: 'booking', component: BeauticianListView, meta: { public: true, title: '预约' } },
        { path: 'orders', component: OrderListView, meta: { title: '我的预约' } },
        { path: 'mine', component: MineView, meta: { title: '我的' } }
      ]
    },
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { public: true, title: '页面不存在' } }
  ]
})

router.onError(() => {
  document.title = '页面加载失败 - 掌美预约'
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path === '/login' && user.token) {
    return '/home'
  }
  const isPublic = to.matched.some((record) => record.meta.public)
  if (!isPublic && !user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 掌美预约` : '掌美预约'
})

export default router
