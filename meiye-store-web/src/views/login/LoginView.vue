<template>
  <div class="login-page">
    <el-card class="card">
      <h1>掌美店管</h1>
      <p>门店运营后台</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="submit">
        <el-form-item prop="mobile">
          <el-input v-model="form.mobile" maxlength="11" placeholder="手机号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ mobile: '', password: '' })
const rules = {
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await user.login(form)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.push(redirect)
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--page-bg);
}
.card {
  width: 380px;
}
h1 {
  margin: 0 0 8px;
  font-size: 24px;
}
p {
  margin: 0 0 24px;
  color: #667085;
}
.submit {
  width: 100%;
}
</style>
