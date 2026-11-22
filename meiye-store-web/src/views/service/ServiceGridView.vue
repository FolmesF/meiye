<template>
  <el-card shadow="never">
    <template #header>
      <div class="head">
        <span>项目管理</span>
        <el-pagination
          v-model:current-page="page"
          :page-size="16"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </template>
    <div v-loading="loading" class="grid">
      <button v-for="item in records" :key="item.id" type="button" class="cell" @click="open(item)">
        {{ item.name }}
      </button>
    </div>
    <el-dialog v-model="visible" :title="detail?.name || '项目'" width="760px">
      <div class="staff">
        <span>可选美容师</span>
        <el-select v-model="selectedIds" multiple filterable collapse-tags placeholder="添加美容师" style="width: 420px">
          <el-option v-for="item in beauticians" :key="item.id" :label="item.name" :value="String(item.id)" />
        </el-select>
        <el-button type="primary" :loading="saving" @click="saveBeauticians">保存</el-button>
      </div>
      <el-table :data="detailRows">
        <el-table-column prop="duration" label="项目时长" width="110" />
        <el-table-column prop="price" label="花费金额" width="120" />
        <el-table-column prop="introduction" label="项目介绍" min-width="160" />
        <el-table-column prop="beauticians" label="可选美容师" min-width="140" />
        <el-table-column prop="material" label="使用库存量" min-width="160" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http, { asRecords } from '../../api/http'
import { useUserStore } from '../../stores/user'

const user = useUserStore()
const loading = ref(false)
const records = ref([])
const page = ref(1)
const total = ref(0)
const visible = ref(false)
const detail = ref(null)
const beauticians = ref([])
const selectedIds = ref([])
const saving = ref(false)

const detailRows = computed(() => {
  if (!detail.value) return []
  const base = {
    duration: `${detail.value.durationMin || 0} 分钟`,
    price: `¥${Number(detail.value.price || 0).toFixed(2)}`,
    introduction: detail.value.introduction || '—',
    beauticians: detail.value.beauticians || '—'
  }
  const materials = detail.value.materials || []
  if (!materials.length) {
    return [{ ...base, material: '—' }]
  }
  return materials.map((item) => ({
    ...base,
    material: `${item.productName} ${item.quantity}`
  }))
})

async function load() {
  loading.value = true
  try {
    const data = asRecords(await http.get('/service-items/cards', {
      params: { storeId: user.profile?.storeId, page: page.value, size: 16 }
    }))
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function open(item) {
  const [card, staff] = await Promise.all([
    http.get(`/service-items/${item.id}/card`),
    http.get('/beauticians/board', { params: { storeId: user.profile?.storeId } })
  ])
  detail.value = card
  beauticians.value = staff || []
  selectedIds.value = (card.beauticianIds || []).map((id) => String(id))
  visible.value = true
}

async function saveBeauticians() {
  if (!detail.value) return
  saving.value = true
  try {
    await http.put(`/service-items/${detail.value.id}/beauticians`, {
      beauticianIds: selectedIds.value
    })
    detail.value = await http.get(`/service-items/${detail.value.id}/card`)
    selectedIds.value = (detail.value.beauticianIds || []).map((id) => String(id))
    ElMessage.success('可选美容师已保存')
  } finally {
    saving.value = false
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
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  min-height: 240px;
}
.cell {
  height: 88px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  font-size: 16px;
  cursor: pointer;
}
.cell:hover {
  border-color: #409eff;
  color: #409eff;
}
.staff {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
