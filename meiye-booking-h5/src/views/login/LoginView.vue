<template>
  <div class="page">
    <nav-bar title="登录" />
    <h1>掌美预约</h1>
    <p class="desc">使用手机号和验证码登录</p>
    <van-form @submit="submit">
    <van-cell-group inset>
      <van-field v-model="form.mobile" type="tel" maxlength="11" label="手机号" placeholder="请输入手机号" :rules="[{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]" />
      <van-field v-model="form.code" type="digit" maxlength="6" label="验证码" placeholder="6 位验证码" :rules="[{ pattern: /^\d{6}$/, message: '请输入 6 位验证码' }]">
        <template #button>
          <van-button size="small" type="primary" :disabled="countdown > 0 || sending" @click="sendCode">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </van-button>
        </template>
      </van-field>
    </van-cell-group>
    <div v-if="debugCode" class="debug">调试验证码 {{ debugCode }}</div>
    <div class="action">
      <van-button block type="primary" native-type="submit" :loading="loading">登录</van-button>
    </div>
    </van-form>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { toast } from '../../utils/feedback'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const debugCode = ref('')
const form = reactive({ mobile: '', code: '' })
let timer = 0

function validMobile() {
  return /^1\d{10}$/.test(form.mobile)
}

async function sendCode() {
  if (!validMobile()) {
    toast('请输入 11 位手机号')
    return
  }
  sending.value = true
  try {
    const data = await user.sendCode(form.mobile)
    debugCode.value = data?.code || ''
    toast('验证码已发送')
    countdown.value = 60
    timer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        window.clearInterval(timer)
      }
    }, 1000)
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    sending.value = false
  }
}

async function submit() {
  if (!validMobile()) {
    toast('请输入 11 位手机号')
    return
  }
  if (!/^\d{6}$/.test(form.code)) {
    toast('请输入 6 位验证码')
    return
  }
  loading.value = true
  try {
    await user.login({ mobile: form.mobile, code: form.code })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(redirect)
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
}
h1 {
  margin: 32px 16px 8px;
  font-size: 28px;
}
.desc {
  margin: 0 16px 24px;
  color: #969799;
  font-size: 14px;
}
.debug {
  margin: 12px 16px 0;
  color: #969799;
  font-size: 13px;
}
.action {
  margin: 24px 16px 0;
}
</style>
