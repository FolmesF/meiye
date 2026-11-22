import { defineStore } from 'pinia'
import http, { setToken } from '../api/http'
import { recognizeRole, roleNames } from '../router/menus'

const PROFILE_KEY = 'meiye_staff_profile'
const PERM_KEY = 'meiye_staff_permissions'

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function normalizePermissions(value) {
  if (Array.isArray(value)) return value
  if (value instanceof Set) return [...value]
  if (value && typeof value === 'object') return Object.values(value)
  return []
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('meiye_staff_token') || '',
    profile: readJson(PROFILE_KEY, null),
    permissions: normalizePermissions(readJson(PERM_KEY, []))
  }),
  getters: {
    role: (state) => recognizeRole(state.profile?.jobType, state.permissions),
    roleName() {
      return roleNames[this.role] || '员工'
    },
    hasPermission: (state) => (code) => {
      if (!code) return true
      return state.permissions.includes(code)
    },
    hasAny: (state) => (codes) => codes.some((code) => state.permissions.includes(code))
  },
  actions: {
    async login(payload) {
      const data = await http.post('/auth/login', payload)
      const permissions = normalizePermissions(data.permissions)
      const role = recognizeRole(data.jobType, permissions)
      this.token = data.token
      this.profile = {
        employeeId: data.employeeId,
        name: data.name,
        mobile: payload.mobile,
        jobType: role || data.jobType || ''
      }
      this.permissions = permissions
      setToken(data.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
      localStorage.setItem(PERM_KEY, JSON.stringify(permissions))
      return this.profile
    },
    clearSession() {
      this.token = ''
      this.profile = null
      this.permissions = []
      setToken('')
      localStorage.removeItem(PROFILE_KEY)
      localStorage.removeItem(PERM_KEY)
    }
  }
})
