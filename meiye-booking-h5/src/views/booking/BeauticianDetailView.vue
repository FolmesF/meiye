<template>
  <div class="page">
    <nav-bar :title="detail?.name || '预约'" left-arrow />
    <template v-if="detail">
      <section class="profile">
        <van-image round width="64" height="64" :src="detail.avatar || ''" fit="cover">
          <template #error>{{ detail.name.slice(0, 1) }}</template>
        </van-image>
        <div>
          <h1>{{ detail.name }}</h1>
          <p>{{ detail.level || '美容师' }} · 工龄 {{ detail.years == null ? '—' : `${detail.years}年` }}</p>
          <p>评分 {{ detail.score == null ? '暂无' : detail.score }}（{{ detail.reviewCount }} 条）</p>
        </div>
      </section>

      <van-cell-group inset title="服务项目">
        <van-cell
          v-for="item in detail.services"
          :key="item.id"
          :title="item.name"
          :label="`${item.durationMin} 分钟`"
          :value="money(item.price)"
          clickable
          @click="selectService(item.id)"
        >
          <template #right-icon>
            <van-icon v-if="String(item.id) === serviceItemId" name="success" color="#1989fa" />
          </template>
        </van-cell>
      </van-cell-group>

      <section class="block">
        <h2>选择日期</h2>
        <div class="dates">
          <button
            v-for="item in dates"
            :key="item.value"
            type="button"
            :class="{ active: item.value === date }"
            @click="changeDate(item.value)"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.day }}</strong>
          </button>
        </div>
      </section>

      <section class="block">
        <h2>选择时段</h2>
        <p v-if="board && !board.onDuty" class="hint">当天未排班</p>
        <div v-else class="slots">
          <button
            v-for="item in board?.slots || []"
            :key="item.start"
            type="button"
            :class="[item.status.toLowerCase(), { picked: item.start === selected }]"
            @click="pick(item)"
          >
            {{ item.start }}
            <small>{{ slotText(item.status) }}</small>
          </button>
        </div>
      </section>

      <van-cell-group inset title="用户评价">
        <van-empty v-if="!reviews.length" description="还没有评价" />
        <van-cell v-for="item in reviews" :key="item.id" :title="item.nickname" :label="item.content || '用户未填写文字'">
          <template #value>门店 {{ item.storeScore || '—' }} 分 · 美容师 {{ item.beauticianScore }} 分</template>
        </van-cell>
      </van-cell-group>
    </template>
    <div class="submit">
      <van-button block type="primary" :loading="submitting" @click="submit">提交预约</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { toast } from '../../utils/feedback'
import { useUserStore } from '../../stores/user'
import { beauticianDetail, beauticianReviews, createAppointment, slotBoard } from '../../api/booking'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const detail = ref(null)
const reviews = ref([])
const board = ref(null)
const serviceItemId = ref(String(route.query.serviceItemId || ''))
const date = ref(String(route.query.date || today()))
const selected = ref(String(route.query.time || ''))
const submitting = ref(false)
const dates = buildDates()

function today() {
  return formatDate(new Date())
}

function formatDate(value) {
  const month = `${value.getMonth() + 1}`.padStart(2, '0')
  const day = `${value.getDate()}`.padStart(2, '0')
  return `${value.getFullYear()}-${month}-${day}`
}

function buildDates() {
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return Array.from({ length: 7 }, (_, index) => {
    const value = new Date()
    value.setDate(value.getDate() + index)
    return {
      value: formatDate(value),
      day: `${value.getMonth() + 1}/${value.getDate()}`,
      label: index === 0 ? '今天' : index === 1 ? '明天' : weeks[value.getDay()]
    }
  })
}

function money(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function slotText(status) {
  return { OPEN: '可约', FULL: '已满', MINE: '已约' }[status] || ''
}

async function loadDetail() {
  detail.value = await beauticianDetail(route.params.id)
  reviews.value = await beauticianReviews(route.params.id)
  if (!serviceItemId.value && detail.value.services?.length) {
    serviceItemId.value = String(detail.value.services[0].id)
  }
}

async function loadSlots() {
  if (!serviceItemId.value) {
    board.value = null
    return
  }
  board.value = await slotBoard({
    beauticianId: route.params.id,
    serviceItemId: serviceItemId.value,
    date: date.value
  })
  if (selected.value && !board.value.slots?.some((item) => item.start === selected.value && item.status === 'OPEN')) {
    selected.value = ''
  }
}

function selectService(id) {
  serviceItemId.value = String(id)
  selected.value = ''
  loadSlots().catch(() => {
    board.value = null
  })
}

function changeDate(value) {
  date.value = value
  selected.value = ''
  loadSlots().catch(() => {
    board.value = null
  })
}

function pick(item) {
  if (item.status !== 'OPEN') {
    toast(item.status === 'MINE' ? '你已预约该时段' : '该时段已满')
    return
  }
  selected.value = item.start
}

async function submit() {
  if (!serviceItemId.value || !selected.value) {
    toast('请选择项目和可约时段')
    return
  }
  const redirect = router.resolve({
    path: route.path,
    query: { ...route.query, serviceItemId: serviceItemId.value, date: date.value, time: selected.value }
  }).fullPath
  if (!user.token) {
    router.push({ path: '/login', query: { redirect } })
    return
  }
  submitting.value = true
  try {
    const latest = await slotBoard({
      beauticianId: route.params.id,
      serviceItemId: serviceItemId.value,
      date: date.value
    })
    board.value = latest
    const cell = (latest.slots || []).find((item) => item.start === selected.value)
    if (!cell || cell.status !== 'OPEN') {
      selected.value = ''
      toast('时段已变化，请重新选择')
      return
    }
    await createAppointment({
      beauticianId: route.params.id,
      serviceItemId: serviceItemId.value,
      appointTime: `${date.value}T${selected.value}:00`
    })
    toast('预约成功')
    router.replace('/orders')
  } catch {
    // 冲突和登录失效由请求拦截器提示，服务端会校验占用并刷新 Redis 版本
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    await loadDetail()
    await loadSlots()
  } catch {
    detail.value = null
  }
})
</script>

<style scoped>
.page {
  padding-bottom: 72px;
}
.profile {
  display: flex;
  gap: 12px;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
}
h1 {
  margin: 0 0 6px;
  font-size: 20px;
}
p {
  margin: 0 0 4px;
  color: #646566;
  font-size: 13px;
}
.block {
  margin: 16px 12px;
}
h2 {
  margin: 0 0 8px;
  font-size: 16px;
}
.dates,
.slots {
  display: flex;
  gap: 8px;
}
.dates {
  overflow-x: auto;
}
.dates button,
.slots button {
  border: 0;
  border-radius: 8px;
  background: #fff;
  color: #323233;
}
.dates button {
  flex: none;
  width: 64px;
  padding: 8px 0;
}
.dates button span,
.slots small {
  display: block;
  color: #969799;
  font-size: 12px;
}
.dates button.active,
.slots button.picked {
  background: #1989fa;
  color: #fff;
}
.dates button.active span,
.slots button.picked small {
  color: #fff;
}
.slots {
  flex-wrap: wrap;
}
.slots button {
  width: calc(33.33% - 6px);
  padding: 8px 0;
}
.slots button.full {
  color: #c8c9cc;
  background: #f2f3f5;
}
.slots button.mine {
  color: #ed6a0c;
  background: #fff7e8;
}
.hint {
  color: #969799;
}
.submit {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 8px 16px calc(8px + env(safe-area-inset-bottom));
  background: #fff;
}
</style>
