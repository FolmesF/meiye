<template>
  <div class="page">
    <el-card shadow="never" class="filter-card">
      <el-form :model="query" inline @submit.prevent="search">
        <el-form-item label="姓名/手机号">
          <el-input v-model="query.keyword" clearable placeholder="模糊搜索" style="width: 200px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="query.level" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="item in levels" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="query.createdRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-permit="'customer'" type="primary" @click="openCreate">新增顾客</el-button>
        <el-button v-permit="'customer'" :disabled="!selection.length" @click="removeBatch">批量删除</el-button>
        <el-button :loading="exporting" @click="exportExcel">导出 Excel</el-button>
      </div>
      <el-table v-loading="loading" :data="records" row-key="id" @selection-change="onSelect">
        <el-table-column type="selection" width="48" />
        <el-table-column label="序号" width="70">
          <template #default="{ $index }">{{ (page - 1) * size + $index + 1 }}</template>
        </el-table-column>
        <el-table-column label="头像" width="72">
          <template #default="{ row }">
            <el-avatar :size="32">{{ avatarText(row.name) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="accountNo" label="账户号" min-width="150" />
        <el-table-column prop="mobile" label="手机号" min-width="130" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="level" label="会员等级" min-width="110">
          <template #default="{ row }">{{ row.level || '—' }}</template>
        </el-table-column>
        <el-table-column label="累计消费金额" min-width="130">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="末次到店时间" min-width="170">
          <template #default="{ row }">{{ dateTime(row.lastVisitAt) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="170">
          <template #default="{ row }">{{ dateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DISABLED' ? 'info' : 'success'">{{ row.status === 'DISABLED' ? '已禁用' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button v-permit="'customer'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-permit="'customer'" link type="danger" @click="removeOne(row)">删除</el-button>
            <el-button v-permit="'customer'" link type="warning" @click="toggleStatus(row)">{{ row.status === 'DISABLED' ? '启用' : '禁用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑顾客' : '新增顾客'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" maxlength="20" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" clearable placeholder="请选择" style="width: 100%">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="会员等级" prop="level">
          <el-select v-model="form.level" clearable filterable allow-create default-first-option placeholder="请选择" style="width: 100%">
            <el-option v-for="item in levels" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="form.address" maxlength="255" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" maxlength="255" show-word-limit :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="顾客详情" width="640px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="姓名">{{ detail.customer.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.customer.mobile }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderText(detail.customer.gender) }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">{{ detail.customer.level || '—' }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ detail.customer.birthday || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.customer.status === 'DISABLED' ? '已禁用' : '正常' }}</el-descriptions-item>
        <el-descriptions-item label="累计消费">{{ money(detail.customer.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="消费次数">{{ detail.customer.visitCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="末次到店">{{ dateTime(detailLastVisit) }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ dateTime(detail.customer.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{ detail.customer.address || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.customer.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">{{ detail.tags?.length ? detail.tags.join('、') : '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const levels = ['普通会员', '银卡会员', '金卡会员', '钻石会员']
const user = useUserStore()
const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const selection = ref([])
const query = reactive({ keyword: '', level: '', createdRange: [] })
const formVisible = ref(false)
const formRef = ref()
const form = reactive(emptyForm())
const detailVisible = ref(false)
const detail = ref(null)
const detailLastVisit = ref('')

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }, { max: 64, message: '姓名不超过 64 字', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^[0-9]{6,20}$/, message: '手机号为 6 到 20 位数字', trigger: 'blur' }
  ],
  level: [{ max: 32, message: '会员等级不超过 32 字', trigger: 'change' }],
  address: [{ max: 255, message: '联系地址不超过 255 字', trigger: 'blur' }],
  remark: [{ max: 255, message: '备注不超过 255 字', trigger: 'blur' }]
}

const storeId = computed(() => user.profile?.storeId)

function emptyForm() {
  return { id: '', name: '', mobile: '', gender: '', birthday: '', level: '', address: '', remark: '' }
}

function avatarText(name) {
  return (name || '?').slice(0, 1)
}

function genderText(value) {
  return { MALE: '男', FEMALE: '女', UNKNOWN: '未知' }[value] || '—'
}

function money(value) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function dateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 19)
}

function params(pageNo, pageSize) {
  const range = query.createdRange || []
  return {
    storeId: storeId.value,
    keyword: query.keyword.trim() || undefined,
    level: query.level || undefined,
    createdFrom: range[0] || undefined,
    createdTo: range[1] || undefined,
    page: pageNo,
    size: pageSize
  }
}

async function load() {
  if (!storeId.value) return
  loading.value = true
  try {
    const data = await http.get('/customers', { params: params(page.value, size.value) })
    records.value = data.records || []
    total.value = Number(data.total || 0)
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function reset() {
  query.keyword = ''
  query.level = ''
  query.createdRange = []
  search()
}

function onSelect(rows) {
  selection.value = rows
}

function openCreate() {
  Object.assign(form, emptyForm())
  formVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    mobile: row.mobile,
    gender: row.gender || '',
    birthday: row.birthday || '',
    level: row.level || '',
    address: row.address || '',
    remark: row.remark || ''
  })
  formVisible.value = true
}

async function openDetail(row) {
  const data = await http.get(`/customers/${row.id}`)
  detail.value = data
  detailLastVisit.value = row.lastVisitAt || ''
  detailVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  const payload = {
    name: form.name.trim(),
    mobile: form.mobile.trim(),
    gender: form.gender || null,
    birthday: form.birthday || null,
    level: form.level || null,
    address: form.address.trim() || null,
    remark: form.remark.trim() || null
  }
  try {
    if (form.id) {
      await http.put(`/customers/${form.id}`, payload)
    } else {
      await http.post('/customers', { ...payload, storeId: storeId.value })
    }
    formVisible.value = false
    await load()
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    saving.value = false
  }
}

async function removeOne(row) {
  try {
    await ElMessageBox.confirm(`删除顾客「${row.name}」？`, '删除确认', { type: 'warning' })
    await http.delete(`/customers/${row.id}`)
    await load()
  } catch {
    // 取消确认时不提交
  }
}

async function removeBatch() {
  try {
    await ElMessageBox.confirm(`删除选中的 ${selection.value.length} 名顾客？`, '批量删除', { type: 'warning' })
    await http.post('/customers/batch-delete', {
      storeId: storeId.value,
      ids: selection.value.map((item) => item.id)
    })
    selection.value = []
    await load()
  } catch {
    // 取消确认时不提交
  }
}

async function toggleStatus(row) {
  const next = row.status === 'DISABLED' ? 'ACTIVE' : 'DISABLED'
  const action = next === 'DISABLED' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`${action}顾客「${row.name}」？`, `${action}确认`, { type: 'warning' })
    await http.put(`/customers/${row.id}/status`, { status: next })
    await load()
  } catch {
    // 取消确认时不提交
  }
}

async function exportExcel() {
  exporting.value = true
  try {
    const rows = []
    let pageNo = 1
    let fetched = 0
    let count = 0
    do {
      const data = await http.get('/customers', { params: params(pageNo, 100) })
      const batch = data.records || []
      count = Number(data.total || 0)
      rows.push(...batch)
      fetched += batch.length
      pageNo += 1
    } while (fetched < count && fetched < 5000 && pageNo < 51)
    const header = ['姓名', '手机号', '性别', '会员等级', '累计消费金额', '末次到店时间', '注册时间', '联系地址', '备注', '状态']
    const body = rows.map((row) => [
      row.name,
      row.mobile,
      genderText(row.gender),
      row.level || '',
      Number(row.totalAmount || 0).toFixed(2),
      dateTime(row.lastVisitAt) === '—' ? '' : dateTime(row.lastVisitAt),
      dateTime(row.createdAt) === '—' ? '' : dateTime(row.createdAt),
      row.address || '',
      row.remark || '',
      row.status === 'DISABLED' ? '已禁用' : '正常'
    ])
    const table = [header, ...body].map((line) => `<tr>${line.map((cell) => `<td>${escapeCell(cell)}</td>`).join('')}</tr>`).join('')
    const html = `<html><head><meta charset="utf-8"></head><body><table>${table}</table></body></html>`
    const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '顾客管理.xls'
    link.click()
    URL.revokeObjectURL(link.href)
  } catch {
    // 错误已由请求拦截器提示
  } finally {
    exporting.value = false
  }
}

function escapeCell(value) {
  return String(value ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

onMounted(load)
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
