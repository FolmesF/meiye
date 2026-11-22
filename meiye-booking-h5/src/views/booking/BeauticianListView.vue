<template>
  <div class="page">
    <van-dropdown-menu>
      <van-dropdown-item v-model="storeId" :options="storeOptions" @change="onStoreChange" />
      <van-dropdown-item v-model="serviceItemId" :options="serviceOptions" @change="load" />
    </van-dropdown-menu>
    <van-pull-refresh v-model="refreshing" @refresh="load">
    <van-empty v-if="error" :description="error" />
    <van-empty v-else-if="!loading && !records.length" description="请选择门店和项目，或暂无美容师" />
    <div v-else class="list">
      <article v-for="item in records" :key="item.id" class="card" @click="open(item)">
        <van-image round width="56" height="56" :src="item.avatar || ''" fit="cover">
          <template #error>{{ (item.name || '美').slice(0, 1) }}</template>
        </van-image>
        <div class="body">
          <div class="row">
            <strong>{{ item.name }}</strong>
            <van-tag :type="tagType(item.bookable)">{{ bookableText(item.bookable) }}</van-tag>
          </div>
          <div class="meta">工龄 {{ item.years == null ? '—' : `${item.years}年` }} · 评分 {{ item.score == null ? '暂无' : item.score }}</div>
          <div class="skills">擅长 {{ item.skills?.length ? item.skills.join('、') : '—' }}</div>
        </div>
      </article>
    </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listBeauticians, listServices, listStores } from '../../api/booking'

const router = useRouter()
const loading = ref(false)
const refreshing = ref(false)
const error = ref('')
const stores = ref([])
const services = ref([])
const records = ref([])
const storeId = ref('')
const serviceItemId = ref('')

const storeOptions = computed(() => [
  { text: '选择门店', value: '' },
  ...stores.value.map((item) => ({ text: item.name, value: String(item.id) }))
])
const serviceOptions = computed(() => [
  { text: '选择项目', value: '' },
  ...services.value.map((item) => ({ text: item.name, value: String(item.id) }))
])

function bookableText(value) {
  return { OPEN: '可约', FULL: '已满', OFF: '休息' }[value] || '未知'
}

function tagType(value) {
  return { OPEN: 'success', FULL: 'danger', OFF: 'default' }[value] || 'default'
}

function today() {
  const date = new Date()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

async function loadStores() {
  stores.value = await listStores()
}

async function onStoreChange() {
  serviceItemId.value = ''
  records.value = []
  services.value = storeId.value ? await listServices(storeId.value) : []
}

async function load() {
  if (!storeId.value || !serviceItemId.value) {
    records.value = []
    refreshing.value = false
    return
  }
  loading.value = true
  error.value = ''
  try {
    const data = await listBeauticians({
      storeId: storeId.value,
      serviceItemId: serviceItemId.value,
      date: today()
    }, { silent: true })
    records.value = Array.isArray(data) ? data : []
  } catch (err) {
    records.value = []
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function open(item) {
  router.push({
    path: `/beauticians/${item.id}`,
    query: { storeId: storeId.value, serviceItemId: serviceItemId.value }
  })
}

onMounted(async () => {
  try {
    await loadStores()
  } catch {
    stores.value = []
  }
})
</script>

<style scoped>
.list {
  padding: 12px;
}
.card {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
}
.body {
  flex: 1;
  min-width: 0;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.meta,
.skills {
  margin-top: 6px;
  color: #646566;
  font-size: 13px;
}
.skills {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
