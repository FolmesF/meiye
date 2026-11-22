<template>
  <div class="layout">
    <div class="content">
      <router-view />
    </div>
    <van-tabbar route>
      <van-tabbar-item v-for="item in tabs" :key="item.path" :to="item.path" :icon="item.icon">
        {{ item.title }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import { visibleTabs } from '../router/menus'

const user = useUserStore()
const tabs = computed(() => visibleTabs(user.hasAny))
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.content {
  padding-bottom: 50px;
}
</style>
