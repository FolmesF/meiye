<template>
  <div>
    <van-nav-bar title="陌拜记录" left-arrow @click-left="router.back()">
      <template #right>
        <span v-permit="'prospect:write'" class="add" @click="router.push('/prospects/new')">新增</span>
      </template>
    </van-nav-bar>
    <van-pull-refresh v-model="refreshing" @refresh="reload">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="records.length ? '没有更多了' : ''" @load="load">
        <van-empty v-if="error" :description="error" />
        <van-empty v-else-if="finished && !records.length" description="还没有陌拜记录" />
        <article v-for="item in records" :key="item.id" class="card">
          <div class="row">
            <strong>{{ item.name }}</strong>
            <van-tag :type="tagType(item.status)">{{ statusText(item.status) }}</van-tag>
          </div>
          <p>拜访时间 {{ dateTime(item.visitedAt) }}</p>
          <p class="remark">跟进备注 {{ item.remark || '—' }}</p>
        </article>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { asRecords } from '../../api/http'
import { listProspects } from '../../api/sales'

const router = useRouter()
const records = ref([])
const page = ref(1)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const error = ref('')

const statusMap = { NEW: '新拜访', FOLLOWING: '跟进中', DONE: '已完成' }

function statusText(status) {
  return statusMap[status] || status || '—'
}

function tagType(status) {
  return { NEW: 'primary', FOLLOWING: 'warning', DONE: 'success' }[status] || 'default'
}

function dateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '—'
}

async function load() {
  try {
    const data = asRecords(await listProspects(page.value, { silent: true }))
    const batch = data.records
    records.value.push(...batch)
    if (records.value.length >= Number(data.total || 0) || batch.length === 0) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (err) {
    error.value = err.message || '加载失败'
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function reload() {
  page.value = 1
  records.value = []
  error.value = ''
  finished.value = false
  loading.value = true
  load()
}
</script>

<style scoped>
.add {
  color: #1989fa;
}
.card {
  margin: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
p {
  margin: 8px 0 0;
  color: #646566;
  font-size: 13px;
}
.remark {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
