<template>
  <div class="page">
    <van-pull-refresh v-model="refreshing" @refresh="reload">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="records.length ? '没有更多了' : ''" @load="load">
        <van-empty v-if="finished && !records.length" description="还没有预约" />
        <van-cell-group v-else inset>
          <van-cell
            v-for="item in records"
            :key="item.id"
            :title="dateTime(item.appointTime)"
            :label="`门店 ${item.storeId} · 美容师 ${item.beauticianId}`"
            :value="statusText(item.status)"
          />
        </van-cell-group>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import http from '../../api/http'

const records = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)

const statusMap = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  ARRIVED: '已到店',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '未到店'
}

function statusText(status) {
  return statusMap[status] || status || '—'
}

function dateTime(value) {
  if (!value) return '预约时间待定'
  return String(value).replace('T', ' ').slice(0, 16)
}

async function load() {
  try {
    const data = await http.get('/me/appointments', { params: { page: page.value, size: 20 } })
    const batch = data.records || []
    records.value.push(...batch)
    if (records.value.length >= Number(data.total || 0) || batch.length === 0) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function reload() {
  page.value = 1
  records.value = []
  finished.value = false
  loading.value = true
  load()
}
</script>

<style scoped>
.page {
  padding-top: 12px;
}
</style>
