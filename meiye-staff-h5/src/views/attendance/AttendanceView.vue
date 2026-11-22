<template>
  <div>
    <van-nav-bar title="考勤" />
    <van-form v-permit="'attendance:clock'" @submit="clock">
      <van-cell-group inset title="打卡">
        <van-field v-model="form.longitude" label="经度" type="number" required placeholder="当前位置经度" :rules="[{ required: true, message: '请填写经度' }]" />
        <van-field v-model="form.latitude" label="纬度" type="number" required placeholder="当前位置纬度" :rules="[{ required: true, message: '请填写纬度' }]" />
      </van-cell-group>
      <div class="action">
        <van-button block type="primary" native-type="submit" :loading="saving">打卡</van-button>
      </div>
    </van-form>
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-cell-group v-if="user.hasPermission('attendance:clock')" inset title="近 7 天考勤">
        <van-empty v-if="error" :description="error" />
        <van-empty v-else-if="!loading && !records.length" description="暂无打卡记录" />
        <van-cell v-for="item in records" :key="item.id" :title="item.workDate || '考勤'" :label="clockText(item)" />
      </van-cell-group>
      <van-cell-group inset>
        <van-cell v-permit="'route:report'" title="行程报岗" is-link to="/routes" />
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'
import { recentRange } from '../../utils/query'

const user = useUserStore()
const form = reactive({ longitude: '', latitude: '' })
const records = ref([])
const loading = ref(false)
const refreshing = ref(false)
const saving = ref(false)
const error = ref('')

function clockText(item) {
  const inn = item.clockInAt ? `上班 ${String(item.clockInAt).replace('T', ' ').slice(11, 16)}` : '未上班打卡'
  const out = item.clockOutAt ? `下班 ${String(item.clockOutAt).replace('T', ' ').slice(11, 16)}` : '未下班打卡'
  return `${inn} · ${out}`
}

async function load() {
  if (!user.hasPermission('attendance:clock')) {
    refreshing.value = false
    return
  }
  loading.value = true
  error.value = ''
  try {
    const data = await http.get('/attendance', { params: recentRange(), silent: true })
    records.value = asRecords(data).records
  } catch (err) {
    records.value = []
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function clock() {
  saving.value = true
  try {
    await http.post('/attendance/clock', {
      longitude: Number(form.longitude),
      latitude: Number(form.latitude)
    })
    showToast('打卡成功')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.action {
  margin: 16px;
}
</style>
