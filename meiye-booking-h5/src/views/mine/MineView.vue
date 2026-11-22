<template>
  <div class="page">
    <div class="profile">
      <van-image round width="64" height="64" :src="user.profile?.avatar || ''" fit="cover">
        <template #error>{{ avatarText }}</template>
      </van-image>
      <div>
        <div class="name">{{ user.profile?.nickname || '未登录' }}</div>
        <div class="mobile">{{ user.profile?.mobile || '登录后查看账号信息' }}</div>
        <div v-if="user.token" class="level">{{ user.profile?.memberLevel || '普通会员' }}</div>
      </div>
    </div>
    <van-cell-group inset>
      <van-cell title="账户信息" is-link to="/profile" />
      <van-cell title="优惠活动" is-link to="/home" />
      <van-cell title="我的预约" is-link to="/orders" />
    </van-cell-group>
    <div class="action">
      <van-button v-if="user.token" block @click="logout">退出登录</van-button>
      <van-button v-else block type="primary" to="/login">去登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { confirm } from '../../utils/feedback'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const user = useUserStore()
const avatarText = computed(() => (user.profile?.nickname || '我').slice(0, 1))

onMounted(async () => {
  if (!user.token) return
  try {
    const data = await http.get('/me', { silent: true })
    user.profile = { ...(user.profile || {}), ...data }
    localStorage.setItem('meiye_booking_profile', JSON.stringify(user.profile))
  } catch {
    // 未登录或接口失败时保留本地资料
  }
})

async function logout() {
  try {
    await confirm({ title: '退出登录', message: '确定退出当前账号？' })
  } catch {
    return
  }
  user.clearSession()
  router.replace('/login')
}
</script>

<style scoped>
.profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 16px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
}
.name {
  font-size: 18px;
  font-weight: 600;
}
.mobile,
.level {
  margin-top: 6px;
  color: #969799;
  font-size: 13px;
}
.action {
  margin: 24px 16px;
}
</style>
