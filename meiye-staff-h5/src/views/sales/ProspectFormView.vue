<template>
  <div class="page">
    <van-nav-bar title="新增陌拜" left-arrow @click-left="router.back()" />
    <van-cell title="客户模板" :value="templateTitle" is-link @click="showTemplates = true" />
    <van-notice-bar v-if="script" wrapable :scrollable="false" :text="`拜访话术：${script}`" />
    <van-form @submit="submit">
      <van-cell-group inset>
        <van-field v-model="form.name" label="客户名称" required placeholder="请输入客户名称" :rules="[{ required: true, message: '请输入客户名称' }]" />
        <van-field v-model="form.mobile" label="手机号" :required="need('mobile')" placeholder="选填" />
        <van-field v-model="form.company" label="单位" :required="need('company')" placeholder="选填" />
        <van-field v-model="form.address" label="地址" :required="need('address')" placeholder="选填" />
        <van-field v-model="form.content" label="拜访内容" type="textarea" rows="3" maxlength="1000" :required="need('content')" placeholder="填写本次拜访内容" />
        <van-field label="现场图片" :required="need('photo')">
          <template #input>
            <van-uploader v-model="files" multiple :max-count="3" :max-size="300 * 1024" @oversize="onOversize" />
          </template>
        </van-field>
        <van-field v-model="form.longitude" label="经度" type="number" :required="need('location')" placeholder="由参数传入" />
        <van-field v-model="form.latitude" label="纬度" type="number" :required="need('location')" placeholder="由参数传入" />
      </van-cell-group>
      <div class="action">
        <van-button block type="primary" native-type="submit" :loading="saving">提交</van-button>
      </div>
    </van-form>
    <van-action-sheet v-model:show="showTemplates" :actions="templateActions" cancel-text="不使用模板" @select="pickTemplate" @cancel="clearTemplate" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createProspect, listTemplates } from '../../api/sales'

const route = useRoute()
const router = useRouter()
const templates = ref([])
const templateId = ref('')
const showTemplates = ref(false)
const files = ref([])
const saving = ref(false)
const form = reactive({
  name: '',
  mobile: '',
  company: '',
  address: '',
  content: '',
  longitude: route.query.longitude ? String(route.query.longitude) : '',
  latitude: route.query.latitude ? String(route.query.latitude) : ''
})

const current = computed(() => templates.value.find((item) => String(item.id) === templateId.value))
const templateTitle = computed(() => current.value?.title || '请选择')
const script = computed(() => current.value?.content || '')
const required = computed(() => new Set((current.value?.requiredFields || '').split(',').filter(Boolean)))
const templateActions = computed(() => templates.value.map((item) => ({ name: item.title, id: item.id })))

function need(key) {
  return required.value.has(key)
}

function pickTemplate(action) {
  templateId.value = String(action.id)
  showTemplates.value = false
}

function clearTemplate() {
  templateId.value = ''
}

function onOversize() {
  showToast('单张图片不超过 300KB')
}

function readFile(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

async function submit() {
  if (need('mobile') && !form.mobile.trim()) return showToast('请填写手机号')
  if (need('company') && !form.company.trim()) return showToast('请填写单位')
  if (need('address') && !form.address.trim()) return showToast('请填写地址')
  if (need('content') && !form.content.trim()) return showToast('请填写拜访内容')
  if (need('photo') && !files.value.length) return showToast('请上传现场图片')
  if (need('location') && (form.longitude === '' || form.latitude === '')) return showToast('请传入定位')
  saving.value = true
  try {
    const photos = []
    for (const item of files.value) {
      photos.push(item.content || await readFile(item.file))
    }
    await createProspect({
      templateId: templateId.value || null,
      name: form.name.trim(),
      mobile: form.mobile.trim() || null,
      company: form.company.trim() || null,
      address: form.address.trim() || null,
      content: form.content.trim() || null,
      photos: photos.length ? JSON.stringify(photos) : null,
      longitude: form.longitude === '' ? null : Number(form.longitude),
      latitude: form.latitude === '' ? null : Number(form.latitude)
    })
    showToast('已提交')
    router.replace('/prospects')
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    templates.value = await listTemplates()
  } catch {
    templates.value = []
  }
})
</script>

<style scoped>
.page {
  padding-bottom: 24px;
}
.action {
  margin: 16px;
}
</style>
