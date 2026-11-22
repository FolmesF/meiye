<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>库存管理</span>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="records">
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="skuCode" label="SKU" min-width="120" />
      <el-table-column label="最大数量" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.maxQuantity" :min="0" :step="1" controls-position="right" @change="saveMax(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="warnQuantity" label="库存预警" width="110" />
      <el-table-column label="剩余数量" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="0" :step="1" controls-position="right" @change="saveQuantity(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="todayConsumed" label="今日消耗" width="110" />
      <el-table-column prop="totalConsumed" label="总消耗" width="110" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const ready = ref(false)
const records = ref([])

async function load() {
  ready.value = false
  loading.value = true
  try {
    records.value = await http.get('/inventory/summary', { params: { storeId: user.profile?.storeId } })
  } finally {
    loading.value = false
    ready.value = true
  }
}

async function saveQuantity(row) {
  if (!ready.value) return
  await http.put(`/inventory/products/${row.productId}/quantity`, {
    storeId: user.profile?.storeId,
    quantity: row.quantity
  })
  ElMessage.success('剩余数量已更新')
}

async function saveMax(row) {
  if (!ready.value) return
  await http.put(`/inventory/products/${row.productId}/max`, { maxQuantity: row.maxQuantity })
  row.warnQuantity = Math.floor(Number(row.maxQuantity || 0) * 0.2)
  ElMessage.success('库存预警已按最大数量的 20% 更新')
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
