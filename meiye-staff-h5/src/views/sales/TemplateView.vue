<template>
  <div>
    <van-nav-bar title="陌拜模板" left-arrow @click-left="router.back()">
      <template #right>
        <span v-permit="'visit-template:manage'" class="add" @click="openCreate">新增</span>
      </template>
    </van-nav-bar>
    <van-empty v-if="!loading && !records.length" description="还没有模板" />
    <van-cell-group v-else inset>
      <van-cell v-for="item in records" :key="item.id" :title="item.title" :label="item.content" is-link @click="openEdit(item)">
        <template #value>{{ requiredText(item.requiredFields) }}</template>
      </van-cell>
    </van-cell-group>

    <van-popup v-model:show="visible" position="bottom" round :style="{ height: '80%' }">
      <div class="editor">
        <h2>{{ form.id ? '编辑模板' : '新增模板' }}</h2>
        <van-form @submit="save">
          <van-field v-model="form.title" label="标题" required maxlength="64" placeholder="模板名称" :rules="[{ required: true, message: '请输入标题' }]" />
          <van-field v-model="form.content" label="拜访话术" type="textarea" rows="4" maxlength="1000" required placeholder="预设拜访时使用的话术" :rules="[{ required: true, message: '请输入话术' }]" />
          <van-cell title="必填项" />
          <van-checkbox-group v-model="form.required">
            <van-cell v-for="item in fieldOptions" :key="item.value" :title="item.label" clickable @click="toggle(item.value)">
              <template #right-icon>
                <van-checkbox :name="item.value" @click.stop />
              </template>
            </van-cell>
          </van-checkbox-group>
          <div class="action">
            <van-button block type="primary" native-type="submit" :loading="saving">保存</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { listTemplates, saveTemplate } from '../../api/sales'

const router = useRouter()
const records = ref([])
const loading = ref(false)
const visible = ref(false)
const saving = ref(false)
const form = reactive({ id: '', title: '', content: '', required: [] })
const fieldOptions = [
  { value: 'mobile', label: '手机号' },
  { value: 'company', label: '单位' },
  { value: 'address', label: '地址' },
  { value: 'content', label: '拜访内容' },
  { value: 'photo', label: '现场图片' },
  { value: 'location', label: '定位' }
]
const labels = Object.fromEntries(fieldOptions.map((item) => [item.value, item.label]))

function requiredText(value) {
  const names = (value || '').split(',').filter(Boolean).map((key) => labels[key] || key)
  return names.length ? names.join('、') : '无额外必填'
}

function openCreate() {
  form.id = ''
  form.title = ''
  form.content = ''
  form.required = []
  visible.value = true
}

function openEdit(item) {
  form.id = item.id
  form.title = item.title
  form.content = item.content
  form.required = (item.requiredFields || '').split(',').filter(Boolean)
  visible.value = true
}

function toggle(value) {
  const index = form.required.indexOf(value)
  if (index >= 0) form.required.splice(index, 1)
  else form.required.push(value)
}

async function load() {
  loading.value = true
  try {
    records.value = await listTemplates()
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await saveTemplate(form.id, {
      title: form.title.trim(),
      content: form.content.trim(),
      requiredFields: form.required.join(',')
    })
    visible.value = false
    showToast('已保存')
    await load()
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.add {
  color: #1989fa;
}
.editor {
  padding: 16px 0 24px;
}
h2 {
  margin: 0 16px 12px;
  font-size: 16px;
}
.action {
  margin: 16px;
}
</style>
