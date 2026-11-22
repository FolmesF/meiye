export const roleNames = {
  GUIDE: '美导',
  SALES: '业务员',
  ADMIN: '管理员'
}

export const roleEntries = {
  GUIDE: [
    { title: '店家管理', desc: '维护负责的门店', path: '/stores', permission: 'store:manage' },
    { title: '巡店记录', desc: '填写巡店记录和位置', path: '/inspections', permission: 'inspection:report' },
    { title: '业绩查看', desc: '按门店和时间查看业绩', path: '/store-performance', permission: 'performance:store' }
  ],
  SALES: [
    { title: '陌拜记录', desc: '登记陌拜客户', path: '/prospects', permission: 'prospect:write' },
    { title: '客户跟进', desc: '记录跟进内容', path: '/followups', permission: 'followup:write' },
    { title: '模板管理', desc: '维护陌拜话术模板', path: '/templates', permission: 'visit-template:manage' }
  ],
  ADMIN: [
    { title: '员工监控', desc: '查看在岗和行程状态', path: '/monitor', permission: 'employee:monitor' },
    { title: '业绩总览', desc: '按人和门店看团队业绩', path: '/team-performance', permission: 'performance:team' },
    { title: '批注管理', desc: '批注员工工作内容', path: '/notes', permission: 'work:annotate' }
  ]
}

export const commonEntries = [
  { title: '考勤打卡', desc: '上班、下班', path: '/attendance', permission: 'attendance:clock' },
  { title: '行程报岗', desc: '上报当前位置和工作内容', path: '/routes', permission: 'route:report' },
  { title: '个人业绩统计', desc: '按时间查看本人业绩', path: '/performance', permission: 'performance:self' }
]

export const appTabs = [
  { path: '/work', title: '工作台', icon: 'apps-o' },
  {
    path: '/performance',
    title: '业绩',
    icon: 'bar-chart-o',
    anyPermission: ['performance:self', 'performance:store', 'performance:team']
  },
  {
    path: '/attendance',
    title: '考勤',
    icon: 'clock-o',
    anyPermission: ['attendance:clock', 'route:report']
  },
  { path: '/mine', title: '我的', icon: 'user-o' }
]

export function recognizeRole(jobType, permissions = []) {
  if (roleNames[jobType]) return jobType
  const codes = new Set(permissions)
  if (['employee:monitor', 'work:annotate', 'performance:team', 'employee:manage'].some((code) => codes.has(code))) {
    return 'ADMIN'
  }
  if (['visit-template:manage', 'prospect:write', 'followup:write'].some((code) => codes.has(code))) {
    return 'SALES'
  }
  if (['store:manage', 'inspection:report', 'performance:store'].some((code) => codes.has(code))) {
    return 'GUIDE'
  }
  return ''
}

export function visibleEntries(entries, hasPermission) {
  return entries.filter((item) => hasPermission(item.permission))
}

export function visibleTabs(hasAny) {
  return appTabs.filter((item) => !item.anyPermission || hasAny(item.anyPermission))
}
