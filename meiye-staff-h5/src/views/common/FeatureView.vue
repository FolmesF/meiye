<template>
  <div>
    <van-nav-bar :title="route.meta.title" left-arrow @click-left="router.back()" />
    <van-pull-refresh v-if="route.meta.api" v-model="refreshing" @refresh="load">
      <van-empty v-if="error" :description="error" />
      <van-empty v-else-if="!loading && !records.length" description="暂无数据" />
      <van-cell-group v-else inset>
        <van-cell v-for="(item, index) in records" :key="item.id || index" :title="titleOf(item)" :label="labelOf(item)" />
      </van-cell-group>
    </van-pull-refresh>
    <van-empty v-else description="请从关联列表进入后再查看明细" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { asRecords } from '../../api/http'
import { monthRange, recentRange } from '../../utils/query'

const route = useRoute()
const router = useRouter()
const records = ref([])
const loading = ref(false)
const refreshing = ref(false)
const error = ref('')

function titleOf(item) {
  return item.name || item.title || item.period || item.employeeName || item.status || '记录'
}

function labelOf(item) {
  const text = item.content || item.address || item.amount || item.workContent || item.mobile || ''
  return text === '' || text === undefined ? '' : String(text)
}

function params() {
  if (route.meta.query === 'performance') {
    return { dimension: 'DAY', ...monthRange() }
  }
  if (route.meta.query === 'dateRange') {
    return recentRange()
  }
  return {}
}

async function load() {
  if (!route.meta.api) return
  loading.value = true
  error.value = ''
  try {
    const data = await http.get(route.meta.api, { params: params(), silent: true })
    records.value = asRecords(data).records
  } catch (err) {
    records.value = []
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

watch(() => route.fullPath, load, { immediate: true })
</script>
