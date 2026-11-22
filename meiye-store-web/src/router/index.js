import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../views/login/LoginView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import ModuleView from '../views/module/ModuleView.vue'
import CustomerView from '../views/customer/CustomerView.vue'
import CampaignView from '../views/campaign/CampaignView.vue'
import AppointmentStatView from '../views/appointment/AppointmentStatView.vue'
import BeauticianBoardView from '../views/beautician/BeauticianBoardView.vue'
import ServiceGridView from '../views/service/ServiceGridView.vue'
import FeeWorkView from '../views/fee/FeeWorkView.vue'
import InventoryBoardView from '../views/inventory/InventoryBoardView.vue'
import ForbiddenView from '../views/error/ForbiddenView.vue'
import NotFoundView from '../views/error/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true, title: '登录' } },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: DashboardView, meta: { title: '首页', fixed: true } },
        { path: 'customers', component: CustomerView, meta: { title: '顾客管理' } },
        { path: 'beauticians', component: BeauticianBoardView, meta: { title: '美容师管理' } },
        { path: 'services', component: ServiceGridView, meta: { title: '项目管理' } },
        { path: 'stats', component: AppointmentStatView, meta: { title: '预约统计' } },
        { path: 'fees', component: FeeWorkView, meta: { title: '手工费核算' } },
        { path: 'inventory', component: InventoryBoardView, meta: { title: '库存管理' } },
        { path: 'mall', component: ModuleView, meta: { title: '美博城', api: '/mall/goods', query: 'page' } },
        { path: 'campaigns', component: CampaignView, meta: { title: '营销活动' } },
        { path: '403', component: ForbiddenView, meta: { title: '无权限', fixed: true } }
      ]
    },
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { public: true, title: '页面不存在' } }
  ]
})

router.onError(() => {
  document.title = '页面加载失败 - 掌美店管'
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.public) {
    if (user.token && to.path === '/login') {
      return '/dashboard'
    }
    return true
  }
  if (!user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const guarded = to.matched.some((record) => record.meta.title) && !to.meta.fixed && to.path !== '/dashboard'
  if (guarded && !user.hasMenu(to.path)) {
    return '/403'
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 掌美店管` : '掌美店管'
})

export default router
