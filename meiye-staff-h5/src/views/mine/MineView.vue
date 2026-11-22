<template>
  <div>
    <van-nav-bar title="我的" />
    <van-cell-group inset>
      <van-cell title="姓名" :value="user.profile?.name || '-'" />
      <van-cell title="岗位" :value="jobLabel" />
      <van-cell title="权限数" :value="String(user.permissions.length)" />
    </van-cell-group>
    <div class="action">
      <van-button block @click="logout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const user = useUserStore()
const jobLabel = computed(() => user.roleName)

function logout() {
  user.clearSession()
  router.replace('/login')
}
</script>

<style scoped>
.action {
  margin: 24px 16px;
}
</style>
