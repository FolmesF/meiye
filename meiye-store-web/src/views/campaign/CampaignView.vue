<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>营销活动</span>
        <div>
          <el-button type="primary" @click="openCreate">新增</el-button>
          <el-button :loading="loading" @click="load">刷新</el-button>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="records">
      <el-table-column prop="name" label="活动名称" min-width="160" />
      <el-table-column prop="campaignType" label="类型" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="活动日期" min-width="220">
        <template #default="{ row }">{{ dateRange(row) }}</template>
      </el-table-column>
      <el-table-column prop="ruleText" label="规则" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :loading="publishingId === row.id" @click="publish(row)">推送到预约端</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="新增营销活动" width="520px">
      <el-form label-width="90px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-input v-model="form.campaignType" maxlength="32" placeholder="例如 会员折扣、充值赠送" />
        </el-form-item>
        <el-form-item label="活动日期" required>
          <el-date-picker
            v-model="form.dates"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="规则">
          <el-input v-model="form.ruleText" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="create">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const records = ref([])
const publishingId = ref('')
const form = reactive({
  name: '',
  campaignType: '',
  dates: [],
  ruleText: ''
})

function dateRange(row) {
  const start = row.startTime ? String(row.startTime).slice(0, 10) : ''
  const end = row.endTime ? String(row.endTime).slice(0, 10) : ''
  return start && end ? `${start} 至 ${end}` : '—'
}

async function load() {
  loading.value = true
  try {
    const data = await http.get('/campaigns', { params: { storeId: user.profile?.storeId, page: 1, size: 50 } })
    records.value = asRecords(data).records
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.name = ''
  form.campaignType = ''
  form.dates = []
  form.ruleText = ''
  visible.value = true
}

async function create() {
  if (!form.name.trim() || !form.campaignType.trim() || !form.dates || form.dates.length !== 2) {
    ElMessage.warning('请填写活动名称、类型和活动日期')
    return
  }
  saving.value = true
  try {
    await http.post('/campaigns', {
      storeId: user.profile?.storeId,
      name: form.name.trim(),
      campaignType: form.campaignType.trim(),
      startDate: form.dates[0],
      endDate: form.dates[1],
      ruleText: form.ruleText
    })
    visible.value = false
    ElMessage.success('营销活动已添加')
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除活动「${row.name}」？`, '删除营销活动')
  } catch {
    return
  }
  await http.delete(`/campaigns/${row.id}`)
  ElMessage.success('已删除')
  await load()
}

async function publish(row) {
  try {
    await ElMessageBox.confirm(`把「${row.name}」推送给预约端用户？同一活动不会重复推送。`, '推送营销活动')
  } catch {
    return
  }
  publishingId.value = row.id
  try {
    const count = await http.post(`/campaigns/${row.id}/publish`)
    ElMessage.success(count > 0 ? `已推送给 ${count} 位用户` : '没有新的接收用户，可能尚未注册或已经推送过')
  } finally {
    publishingId.value = ''
  }
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
