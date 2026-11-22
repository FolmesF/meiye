<template>
  <div>
    <van-nav-bar title="业绩" />
    <van-pull-refresh v-model="refreshing" @refresh="load">
      <van-cell-group v-if="user.hasPermission('performance:self')" inset title="个人业绩">
        <van-empty v-if="error" :description="error" />
        <van-empty v-else-if="!loading && !records.length" description="本月暂无业绩" />
        <van-cell v-for="item in records" :key="item.period" :title="item.period || '合计'" :value="money(item.amount)" :label="`${item.bizCount || 0} 笔`" />
      </van-cell-group>
      <van-cell-group inset>
        <van-cell v-permit="'performance:store'" title="门店业绩" is-link to="/store-performance" />
        <van-cell v-permit="'performance:team'" title="团队业绩总览" is-link to="/team-performance" />
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'
import { monthRange } from '../../utils/query'

const user = useUserStore()
const records = ref([])
const loading = ref(false)
const refreshing = ref(false)
const error = ref('')

function money(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

async function load() {
  if (!user.hasPermission('performance:self')) {
    refreshing.value = false
    return
  }
  loading.value = true
  error.value = ''
  try {
    const data = await http.get('/performances/self', { params: { dimension: 'DAY', ...monthRange() }, silent: true })
    records.value = asRecords(data).records
  } catch (err) {
    records.value = []
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

onMounted(load)
</script>
