<template>
  <el-card shadow="never">
    <template #header>预约统计</template>
    <el-form :inline="true" class="filter" @submit.prevent="load">
      <el-form-item label="时间">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item label="项目">
        <el-input v-model="query.serviceName" clearable placeholder="项目名称" />
      </el-form-item>
      <el-form-item label="美容师">
        <el-input v-model="query.beauticianName" clearable placeholder="美容师" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.mobile" clearable placeholder="手机号" />
      </el-form-item>
      <el-form-item label="预约状态">
        <el-select v-model="query.phase" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="item in phases" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">筛选</el-button>
        <el-button @click="reset">当日</el-button>
      </el-form-item>
    </el-form>
    <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
    <el-table v-else v-loading="loading" :data="records" :row-class-name="rowClass">
      <el-table-column prop="phase" label="预约状态" width="110" />
      <el-table-column prop="appointDate" label="日期" width="120" />
      <el-table-column prop="timeRange" label="具体时间段" width="140" />
      <el-table-column prop="customerName" label="客户名称" min-width="120" />
      <el-table-column prop="accountNo" label="用户账号" min-width="150" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column prop="serviceName" label="项目" min-width="140" />
      <el-table-column prop="beauticianName" label="所选美容师" min-width="120" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const error = ref('')
const records = ref([])
const range = ref([])
const phases = ['等待中', '进行中', '已取消', '已完成']
const query = reactive({ serviceName: '', beauticianName: '', mobile: '', phase: '' })

function rowClass({ row }) {
  return {
    等待中: 'row-wait',
    进行中: 'row-run',
    已取消: 'row-cancel',
    已完成: 'row-done'
  }[row.phase] || ''
}

function params() {
  const value = {
    storeId: user.profile?.storeId,
    serviceName: query.serviceName || undefined,
    beauticianName: query.beauticianName || undefined,
    mobile: query.mobile || undefined,
    phase: query.phase || undefined
  }
  if (range.value?.length === 2) {
    value.startDate = range.value[0]
    value.endDate = range.value[1]
  }
  return value
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await http.get('/appointments/records', { params: params(), silent: true })
    records.value = asRecords(data).records
  } catch (err) {
    records.value = []
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function reset() {
  range.value = []
  query.serviceName = ''
  query.beauticianName = ''
  query.mobile = ''
  query.phase = ''
  load()
}

onMounted(load)
</script>

<style scoped>
.filter {
  margin-bottom: 8px;
}
:deep(.row-wait td) {
  background: #fff;
  color: #303133;
}
:deep(.row-run td) {
  background: #f0f9eb;
  color: #67c23a;
}
:deep(.row-cancel td) {
  background: #fef0f0;
  color: #f56c6c;
}
:deep(.row-done td) {
  background: #ecf5ff;
  color: #409eff;
}
</style>
