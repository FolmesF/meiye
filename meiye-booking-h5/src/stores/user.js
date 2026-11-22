import { defineStore } from 'pinia'
import http, { setToken } from '../api/http'

const PROFILE_KEY = 'meiye_booking_profile'

function readJson(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('meiye_booking_token') || '',
    profile: readJson(PROFILE_KEY)
  }),
  actions: {
    async sendCode(mobile) {
      return http.post('/auth/code', { mobile })
    },
    async login(payload) {
      const data = await http.post('/auth/login', payload)
      this.token = data.token
      this.profile = data
      setToken(data.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(data))
    },
    clearSession() {
      this.token = ''
      this.profile = null
      setToken('')
      localStorage.removeItem(PROFILE_KEY)
    }
  }
})
