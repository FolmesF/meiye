<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>美容师管理</span>
        <div>
          <el-button type="primary" @click="openCreate">新增</el-button>
          <el-button :loading="loading" @click="load">刷新</el-button>
        </div>
      </div>
    </template>
    <el-table v-loading="loading" :data="records">
      <el-table-column prop="name" label="姓名" min-width="120" />
      <el-table-column prop="entryDate" label="入职时间" width="140" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column prop="level" label="等级" width="100" />
      <el-table-column label="综合评分" width="120">
        <template #default="{ row }">{{ row.score == null ? '暂无' : row.score }}</template>
      </el-table-column>
      <el-table-column label="单次费用" width="120">
        <template #default="{ row }">{{ money(row.sessionFee) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="新增美容师" width="480px">
      <el-form label-width="90px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.mobile" maxlength="20" />
        </el-form-item>
        <el-form-item label="入职时间">
          <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" filterable allow-create style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="单次费用">
          <el-input-number v-model="form.sessionFee" :min="0" :precision="2" :step="10" style="width: 100%" />
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
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const records = ref([])
const form = reactive({
  name: '',
  mobile: '',
  entryDate: '',
  level: '初级',
  sessionFee: 128
})

function money(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

async function load() {
  loading.value = true
  try {
    records.value = await http.get('/beauticians/board', { params: { storeId: user.profile?.storeId } })
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.name = ''
  form.mobile = ''
  form.entryDate = new Date().toISOString().slice(0, 10)
  form.level = '初级'
  form.sessionFee = 128
  visible.value = true
}

async function create() {
  if (!form.name.trim() || !/^[0-9]{6,20}$/.test(form.mobile)) {
    ElMessage.warning('请填写姓名和手机号')
    return
  }
  saving.value = true
  try {
    await http.post('/beauticians', {
      storeId: user.profile?.storeId,
      name: form.name.trim(),
      mobile: form.mobile,
      entryDate: form.entryDate || null,
      level: form.level,
      sessionFee: form.sessionFee
    })
    visible.value = false
    ElMessage.success('美容师已添加')
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除美容师「${row.name}」？已关联的项目会同步去掉这位美容师。`, '删除美容师')
  } catch {
    return
  }
  await http.delete(`/beauticians/${row.id}`)
  ElMessage.success('已删除')
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
