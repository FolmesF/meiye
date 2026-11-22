import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import TabLayout from '../layouts/TabLayout.vue'
import LoginView from '../views/login/LoginView.vue'
import WorkView from '../views/work/WorkView.vue'
import PerformanceView from '../views/performance/PerformanceView.vue'
import AttendanceView from '../views/attendance/AttendanceView.vue'
import MineView from '../views/mine/MineView.vue'
import FeatureView from '../views/common/FeatureView.vue'
import ProspectListView from '../views/sales/ProspectListView.vue'
import ProspectFormView from '../views/sales/ProspectFormView.vue'
import TemplateView from '../views/sales/TemplateView.vue'
import ForbiddenView from '../views/error/ForbiddenView.vue'
import NotFoundView from '../views/error/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true, title: '登录' } },
    {
      path: '/',
      component: TabLayout,
      redirect: '/work',
      children: [
        { path: 'work', component: WorkView, meta: { title: '工作台' } },
        {
          path: 'performance',
          component: PerformanceView,
          meta: { title: '业绩', anyPermission: ['performance:self', 'performance:store', 'performance:team'] }
        },
        {
          path: 'attendance',
          component: AttendanceView,
          meta: { title: '考勤', anyPermission: ['attendance:clock', 'route:report'] }
        },
        { path: 'mine', component: MineView, meta: { title: '我的' } },
        { path: 'stores', component: FeatureView, meta: { title: '店家管理', permission: 'store:manage', api: '/stores' } },
        { path: 'inspections', component: FeatureView, meta: { title: '巡店记录', permission: 'inspection:report', api: '' } },
        { path: 'store-performance', component: FeatureView, meta: { title: '业绩查看', permission: 'performance:store', api: '/performances/store', query: 'performance' } },
        { path: 'prospects', component: ProspectListView, meta: { title: '陌拜记录', permission: 'prospect:write' } },
        { path: 'prospects/new', component: ProspectFormView, meta: { title: '新增陌拜', permission: 'prospect:write' } },
        { path: 'followups', component: FeatureView, meta: { title: '客户跟进', permission: 'followup:write', api: '' } },
        { path: 'templates', component: TemplateView, meta: { title: '模板管理', permission: 'visit-template:manage' } },
        { path: 'monitor', component: FeatureView, meta: { title: '员工监控', permission: 'employee:monitor', api: '/team/status' } },
        { path: 'team-performance', component: FeatureView, meta: { title: '业绩总览', permission: 'performance:team', api: '/performances/team', query: 'performance' } },
        { path: 'notes', component: FeatureView, meta: { title: '批注管理', permission: 'work:annotate', api: '' } },
        { path: 'routes', component: FeatureView, meta: { title: '行程报岗', permission: 'route:report', api: '/route-reports', query: 'dateRange' } },
        { path: '403', component: ForbiddenView, meta: { title: '无权限', fixed: true } }
      ]
    },
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { public: true, title: '页面不存在' } }
  ]
})

router.onError(() => {
  document.title = '页面加载失败 - 掌络'
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.path === '/login' && user.token) {
    return '/work'
  }
  const isPublic = to.matched.some((record) => record.meta.public)
  if (!isPublic && !user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.fixed) return true
  if (to.meta.permission && !user.hasPermission(to.meta.permission)) {
    return '/403'
  }
  if (to.meta.anyPermission && !user.hasAny(to.meta.anyPermission)) {
    return '/403'
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 掌络` : '掌络'
})

export default router
