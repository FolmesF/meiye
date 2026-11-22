<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>{{ route.meta.title }}</span>
        <el-button v-if="route.meta.api" :loading="loading" @click="load">刷新</el-button>
      </div>
    </template>
    <el-empty v-if="!route.meta.api" description="该模块暂无列表接口" />
    <el-alert v-else-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-empty v-else-if="!loading && !records.length" description="暂无数据" />
    <el-table v-else v-loading="loading" :data="records">
      <el-table-column v-for="col in columns" :key="col" :prop="col" :label="labelOf(col)" min-width="120" show-overflow-tooltip />
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'
import { monthRange } from '../../utils/query'

const route = useRoute()
const user = useUserStore()
const loading = ref(false)
const error = ref('')
const records = ref([])

const columns = computed(() => {
  const row = records.value[0]
  if (!row || typeof row !== 'object') return []
  return Object.keys(row).filter((key) => !['deleted', 'password'].includes(key)).slice(0, 8)
})

const labels = {
  name: '名称',
  mobile: '手机号',
  status: '状态',
  level: '等级',
  price: '价格',
  category: '分类',
  amount: '金额',
  period: '周期',
  appointmentCount: '预约数',
  arrivedCount: '到店数',
  cancelledCount: '取消数',
  arrivalRate: '到店率',
  cancelRate: '取消率',
  skuCode: 'SKU',
  unit: '单位',
  warnQuantity: '预警值',
  createdAt: '创建时间',
  ruleType: '规则类型',
  fixedAmount: '固定金额',
  rate: '比例',
  brand: '品牌',
  spec: '规格',
  supplier: '供应商',
  campaignType: '活动类型',
  startTime: '开始时间',
  endTime: '结束时间',
  ruleText: '规则'
}

function labelOf(key) {
  return labels[key] || key
}

function params() {
  const storeId = user.profile?.storeId
  const range = monthRange()
  if (route.meta.query === 'stat') {
    return { storeId, dimension: 'DAY', ...range }
  }
  if (route.meta.query === 'dateRange') {
    return { storeId, ...range }
  }
  return { storeId, page: 1, size: 20 }
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
  }
}

watch(() => route.fullPath, load, { immediate: true })
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
