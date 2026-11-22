<template>
  <div>
    <van-tabs v-model:active="group" sticky offset-top="46" @change="reload">
      <van-tab title="待服务" name="WAITING" />
      <van-tab title="已完成" name="DONE" />
      <van-tab title="已取消" name="CANCELLED" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="reload">
      <van-list v-model:loading="loading" :finished="finished" :finished-text="records.length ? '没有更多了' : ''" @load="load">
        <van-empty v-if="error" :description="error" />
        <van-empty v-else-if="finished && !records.length" description="没有预约记录" />
        <article v-for="item in records" :key="item.id" class="card">
          <div class="row">
            <strong>{{ item.serviceName }}</strong>
            <span>{{ statusText(item.status) }}</span>
          </div>
          <p>美容师 {{ item.beauticianName }}</p>
          <p>预约时间 {{ dateTime(item.appointTime) }}</p>
          <div class="actions">
            <van-button v-if="canCancel(item)" v-permit="'login'" size="small" @click="cancel(item)">取消预约</van-button>
            <van-button v-if="item.status === 'COMPLETED' && !item.reviewed" v-permit="'login'" size="small" type="primary" @click="openReview(item)">去评价</van-button>
            <span v-if="item.reviewed" class="done">已评价</span>
          </div>
        </article>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="reviewVisible" position="bottom" round>
      <div class="review">
        <h2>评价 {{ reviewing?.serviceName }}</h2>
        <div class="score">门店 <van-rate v-model="reviewForm.storeScore" /></div>
        <div class="score">美容师 <van-rate v-model="reviewForm.beauticianScore" /></div>
        <div class="score">项目 <van-rate v-model="reviewForm.serviceScore" /></div>
        <van-field v-model="reviewForm.content" type="textarea" maxlength="500" rows="3" placeholder="写下本次服务感受" />
        <van-button block type="primary" :loading="reviewingSaving" @click="submitReview">提交评价</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { toast, confirm } from '../../utils/feedback'
import { asRecords } from '../../api/http'
import { cancelAppointment, createReview, myAppointments } from '../../api/booking'

const group = ref('WAITING')
const records = ref([])
const page = ref(1)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const error = ref('')
const reviewVisible = ref(false)
const reviewing = ref(null)
const reviewingSaving = ref(false)
const reviewForm = reactive({ storeScore: 5, beauticianScore: 5, serviceScore: 5, content: '' })

const statusMap = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  ARRIVED: '已到店',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

function statusText(status) {
  return statusMap[status] || status
}

function dateTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '—'
}

function canCancel(item) {
  return item.status === 'PENDING' || item.status === 'CONFIRMED'
}

async function load() {
  try {
    const data = asRecords(await myAppointments(group.value, page.value, { silent: true }))
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

async function cancel(item) {
  try {
    await confirm({ title: '取消预约', message: `取消 ${item.serviceName} ${dateTime(item.appointTime)}？` })
    await cancelAppointment(item.id, '用户取消')
    toast('已取消')
    reload()
  } catch {
    // 取消确认或接口失败
  }
}

function openReview(item) {
  reviewing.value = item
  reviewForm.storeScore = 5
  reviewForm.beauticianScore = 5
  reviewForm.serviceScore = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.storeScore || !reviewForm.beauticianScore || !reviewForm.serviceScore) {
    toast('请为门店、美容师和项目评分')
    return
  }
  reviewingSaving.value = true
  try {
    await createReview({
      appointmentId: reviewing.value.id,
      beauticianScore: reviewForm.beauticianScore,
      storeScore: reviewForm.storeScore,
      serviceScore: reviewForm.serviceScore,
      content: reviewForm.content.trim()
    })
    reviewVisible.value = false
    toast('评价已提交')
    reload()
  } catch {
    // 拦截器已提示
  } finally {
    reviewingSaving.value = false
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
.row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
p {
  margin: 6px 0 0;
  color: #646566;
  font-size: 13px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
.done {
  color: #969799;
  font-size: 13px;
  line-height: 32px;
}
.review {
  padding: 16px;
}
h2 {
  margin: 0 0 12px;
  font-size: 16px;
}
.score {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
