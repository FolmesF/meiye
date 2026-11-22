<template>
  <div v-loading="loading">
    <h2 class="store">{{ overview.storeName || '门店' }}</h2>
    <el-alert v-if="error" class="alerts" :title="error" type="error" show-icon :closable="false" />
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="never" class="stat">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" class="alerts">
      <template #header>库存预警概览</template>
      <el-table :data="alerts" empty-text="暂无库存预警">
        <el-table-column prop="productName" label="商品" min-width="160" />
        <el-table-column prop="skuCode" label="SKU" min-width="120" />
        <el-table-column prop="quantity" label="现存量" width="100" />
        <el-table-column prop="warnQuantity" label="预警值" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const error = ref('')
const overview = ref({
  storeName: '',
  appointmentCount: 0,
  revenue: 0,
  arrivedCount: 0,
  alerts: []
})

const alerts = computed(() => overview.value.alerts || [])
const cards = computed(() => [
  { label: '今日预约', value: overview.value.appointmentCount ?? 0 },
  { label: '今日营收', value: formatMoney(overview.value.revenue) },
  { label: '到店人数', value: overview.value.arrivedCount ?? 0 },
  { label: '库存预警', value: alerts.value.length }
])

function formatMoney(value) {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (user.profile?.storeId) {
      params.storeId = user.profile.storeId
    }
    overview.value = await http.get('/dashboard/overview', { params, silent: true })
  } catch (err) {
    error.value = err.message || '首页数据加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.store {
  margin: 0 0 16px;
  font-size: 22px;
}
.stat {
  margin-bottom: 16px;
}
.label {
  color: #667085;
  font-size: 14px;
}
.value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
}
.alerts {
  margin-top: 4px;
}
</style>
