<template>
  <div class="page">
    <van-pull-refresh v-model="refreshing" @refresh="reload">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="records.length ? '没有更多了' : ''" @load="load">
        <van-empty v-if="error" :description="error" />
        <van-empty v-else-if="finished && !records.length" description="暂无优惠活动" />
        <article v-for="item in records" :key="item.id" class="card" :class="{ unread: item.readFlag === 0 }" @click="open(item)">
          <div class="row">
            <strong>{{ item.title }}</strong>
            <span>优惠活动</span>
          </div>
          <p>{{ item.content }}</p>
          <time>{{ dateTime(item.createdAt) }}</time>
        </article>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import http, { asRecords } from '../../api/http'
import { toast } from '../../utils/feedback'

const records = ref([])
const page = ref(1)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const error = ref('')

function dateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  try {
    const data = asRecords(await http.get('/messages', {
      params: { page: page.value, size: 20, msgType: 'CAMPAIGN' },
      silent: true
    }))
    records.value.push(...data.records)
    if (records.value.length >= Number(data.total || 0) || data.records.length === 0) {
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

async function open(item) {
  if (item.readFlag === 1) return
  try {
    await http.post(`/messages/${item.id}/read`)
    item.readFlag = 1
  } catch {
    toast('标记已读失败')
  }
}
</script>

<style scoped>
.card {
  margin: 12px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
}
.card.unread {
  box-shadow: inset 3px 0 0 #ee0a24;
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
  line-height: 1.5;
}
time {
  display: block;
  margin-top: 8px;
  color: #969799;
  font-size: 12px;
}
</style>
