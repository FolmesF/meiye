<template>
  <div class="page">
    <h1>掌络</h1>
    <p>行业人员工作台</p>
    <van-form @submit="submit">
      <van-cell-group inset>
        <van-field v-model="form.mobile" type="tel" maxlength="11" label="手机号" placeholder="请输入手机号" :rules="[{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]" />
      </van-cell-group>
      <div class="action">
        <van-button block type="primary" native-type="submit" :loading="loading">登录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '../../stores/user'
import { roleNames } from '../../router/menus'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const form = reactive({ mobile: '' })

async function submit() {
  loading.value = true
  try {
    const profile = await user.login(form)
    showToast(`当前角色：${roleNames[profile.jobType] || '员工'}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/work'
    router.replace(redirect)
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 48px 0 16px;
}
h1 {
  margin: 0 16px;
  font-size: 28px;
}
p {
  margin: 8px 16px 24px;
  color: #969799;
}
.action {
  margin: 24px 16px 0;
}
</style>
