<template>
  <div class="layout">
    <nav-bar :title="title" />
    <div class="content">
      <router-view />
    </div>
    <van-tabbar route safe-area-inset-bottom>
      <van-tabbar-item v-for="item in appTabs" :key="item.path" :to="item.path" :icon="item.icon">
        {{ item.title }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import { appTabs } from '../router/menus'

const route = useRoute()
const title = computed(() => route.meta.title || '掌美预约')
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.content {
  padding-bottom: 50px;
}
</style>
