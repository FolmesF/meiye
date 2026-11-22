<template>
  <el-container class="layout">
    <el-aside :width="app.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="brand">
        <span class="mark">掌</span>
        <span v-show="!app.sidebarCollapsed" class="name">掌美店管</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="app.sidebarCollapsed"
          :collapse-transition="false"
          router
          background-color="#1f2a37"
          text-color="#c9d1d9"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item v-for="item in user.menus" :key="item.path" :index="item.path">
            <el-icon><component :is="menuIcon(item.icon)" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button class="fold" text @click="app.toggleSidebar">
            <el-icon :size="18">
              <Expand v-if="app.sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <span class="title">{{ route.meta.title || '掌美店管' }}</span>
        </div>
        <div class="user">
          <div class="meta">
            <strong>{{ profileName }}</strong>
            <span>{{ user.profile?.mobile }}</span>
          </div>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, Expand, Fold } from '@element-plus/icons-vue'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
import { menuIcon } from '../router/menus'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const user = useUserStore()
const profileName = computed(() => user.profile?.name || '已登录')

async function logout() {
  await user.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  display: flex;
  flex-direction: column;
  background: #1f2a37;
  transition: width 0.2s ease;
  overflow: hidden;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 16px;
  color: #fff;
  font-weight: 600;
  white-space: nowrap;
}
.mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e8eb;
}
.left {
  display: flex;
  align-items: center;
  gap: 4px;
}
.fold {
  padding: 8px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.user {
  display: flex;
  align-items: center;
  gap: 16px;
}
.meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.3;
}
.meta span {
  color: #667085;
  font-size: 12px;
}
.main {
  min-height: 0;
}
.aside :deep(.el-menu) {
  border-right: none;
}
</style>
