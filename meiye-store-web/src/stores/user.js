import { defineStore } from 'pinia'
import http, { setToken } from '../api/http'

const TOKEN_KEY = 'meiye_store_token'
const PROFILE_KEY = 'meiye_store_profile'
const MENU_KEY = 'meiye_store_menus'

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    profile: readJson(PROFILE_KEY, null),
    menus: readJson(MENU_KEY, [])
  }),
  getters: {
    hasMenu: (state) => (path) => state.menus.some((item) => item.path === path),
    hasCode: (state) => (code) => state.menus.some((item) => item.code === code)
  },
  actions: {
    async login(payload) {
      const data = await http.post('/auth/login', payload)
      this.applySession(data)
    },
    async refreshMenus() {
      if (!this.token) return
      const menus = await http.get('/auth/menus')
      this.menus = Array.isArray(menus) ? menus : []
      localStorage.setItem(MENU_KEY, JSON.stringify(this.menus))
    },
    applySession(data) {
      this.token = data.token
      this.profile = {
        name: data.name,
        mobile: data.mobile,
        storeId: data.storeId
      }
      this.menus = data.menus || []
      setToken(data.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
      localStorage.setItem(MENU_KEY, JSON.stringify(this.menus))
    },
    async logout() {
      try {
        await http.post('/auth/logout')
      } catch {
        // 会话已失效时仍清掉本地登录态
      }
      this.clearSession()
    },
    clearSession() {
      this.token = ''
      this.profile = null
      this.menus = []
      setToken('')
      localStorage.removeItem(PROFILE_KEY)
      localStorage.removeItem(MENU_KEY)
    }
  }
})
