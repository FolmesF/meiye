import { defineStore } from 'pinia'

const COLLAPSE_KEY = 'meiye_store_collapsed'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: localStorage.getItem(COLLAPSE_KEY) === '1'
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      localStorage.setItem(COLLAPSE_KEY, this.sidebarCollapsed ? '1' : '0')
    }
  }
})
