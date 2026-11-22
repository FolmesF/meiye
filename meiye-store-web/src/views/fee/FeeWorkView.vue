<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>手工费核算</span>
        <div>
          <el-button type="primary" @click="picker = true">添加预约项目</el-button>
          <el-button :loading="loading" @click="load">刷新</el-button>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="running">
      <el-table-column prop="phase" label="预约状态" width="110" />
      <el-table-column prop="appointDate" label="日期" width="120" />
      <el-table-column prop="timeRange" label="时间段" width="140" />
      <el-table-column prop="customerName" label="客户" min-width="100" />
      <el-table-column prop="serviceName" label="项目" min-width="120" />
      <el-table-column prop="beauticianName" label="美容师" min-width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="finish(row)">结束项目</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="picker" title="添加预约项目" width="760px">
      <el-table :data="waiting">
        <el-table-column prop="appointDate" label="日期" width="120" />
        <el-table-column prop="timeRange" label="时间段" width="140" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="serviceName" label="项目" min-width="120" />
        <el-table-column prop="beauticianName" label="美容师" min-width="100" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" @click="start(row)">添加</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const records = ref([])
const picker = ref(false)
const waiting = computed(() => records.value.filter((item) => item.phase === '等待中'))
const running = computed(() => records.value.filter((item) => item.phase === '进行中'))

async function load() {
  loading.value = true
  try {
    records.value = await http.get('/appointments/workable', { params: { storeId: user.profile?.storeId } })
  } finally {
    loading.value = false
  }
}

async function start(row) {
  await http.post(`/appointments/${row.id}/start`)
  picker.value = false
  await load()
}

async function finish(row) {
  try {
    await ElMessageBox.confirm(`确认完成「${row.serviceName}」？完成后会计入库存消耗。`, '结束项目')
  } catch {
    return
  }
  await http.post(`/appointments/${row.id}/finish`)
  await load()
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
