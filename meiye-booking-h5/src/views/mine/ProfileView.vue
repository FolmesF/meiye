<template>
  <div class="page">
    <van-nav-bar title="账户信息" left-arrow fixed placeholder @click-left="router.back()" />
    <van-form @submit="submit">
      <van-cell-group inset>
        <van-field :model-value="form.accountNo || '保存后生成'" label="账户号" readonly />
        <van-field v-model="form.nickname" label="用户名" maxlength="64" placeholder="请输入用户名" :rules="[{ required: true, message: '请输入用户名' }]" />
        <van-field v-model="form.birthday" label="出生日期" readonly is-link placeholder="请选择" @click="dateVisible = true" />
        <van-field v-model="genderLabel" label="性别" readonly is-link placeholder="请选择" @click="genderVisible = true" />
        <van-field v-model="form.mobile" label="手机号" type="tel" maxlength="11" placeholder="11 位手机号" :rules="[{ pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]" />
        <van-field v-model="form.address" label="联系地址" maxlength="255" type="textarea" rows="2" autosize placeholder="请输入联系地址" />
        <van-field v-model="tagInput" label="标签" maxlength="16" placeholder="输入后添加" @keyup.enter="addTag">
          <template #button>
            <van-button size="small" type="primary" @click="addTag">添加</van-button>
          </template>
        </van-field>
        <div v-if="form.tags.length" class="tags">
          <van-tag v-for="item in form.tags" :key="item" closeable plain type="primary" @close="removeTag(item)">{{ item }}</van-tag>
        </div>
        <van-field :model-value="form.memberLevel || '普通会员'" label="会员等级" readonly />
        <p class="hint">会员等级由门店设置，不能在这里修改</p>
      </van-cell-group>
      <div class="action">
        <van-button block type="primary" native-type="submit" :loading="saving">保存</van-button>
      </div>
    </van-form>

    <van-popup v-model:show="dateVisible" position="bottom">
      <van-date-picker v-model="dateParts" title="出生日期" :min-date="minDate" :max-date="maxDate" @confirm="confirmDate" @cancel="dateVisible = false" />
    </van-popup>
    <van-popup v-model:show="genderVisible" position="bottom">
      <van-picker :columns="genders" @confirm="confirmGender" @cancel="genderVisible = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import { toast } from '../../utils/feedback'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const user = useUserStore()
const saving = ref(false)
const dateVisible = ref(false)
const genderVisible = ref(false)
const tagInput = ref('')
const minDate = new Date(1950, 0, 1)
const maxDate = new Date()
const dateParts = ref(['1990', '01', '01'])
const genders = [
  { text: '女', value: 'FEMALE' },
  { text: '男', value: 'MALE' },
  { text: '未知', value: 'UNKNOWN' }
]
const form = reactive({
  accountNo: '',
  nickname: '',
  birthday: '',
  gender: '',
  mobile: '',
  address: '',
  memberLevel: '普通会员',
  tags: []
})

const genderLabel = computed(() => genders.find((item) => item.value === form.gender)?.text || '')

function confirmDate({ selectedValues }) {
  form.birthday = selectedValues.join('-')
  dateParts.value = selectedValues
  dateVisible.value = false
}

function confirmGender({ selectedOptions }) {
  form.gender = selectedOptions[0]?.value || ''
  genderVisible.value = false
}

function addTag() {
  const tag = tagInput.value.trim()
  if (!tag) return
  if (tag.length > 16) {
    toast('单个标签不超过 16 字')
    return
  }
  if (form.tags.includes(tag)) {
    tagInput.value = ''
    return
  }
  if (form.tags.length >= 8) {
    toast('标签最多 8 个')
    return
  }
  form.tags.push(tag)
  tagInput.value = ''
}

function removeTag(tag) {
  form.tags = form.tags.filter((item) => item !== tag)
}

async function load() {
  const data = await http.get('/me')
  form.accountNo = data.accountNo || ''
  form.nickname = data.nickname || ''
  form.birthday = data.birthday || ''
  form.gender = data.gender || ''
  form.mobile = data.mobile || ''
  form.address = data.address || ''
  form.memberLevel = data.memberLevel || '普通会员'
  form.tags = Array.isArray(data.tags) ? [...data.tags] : []
  if (form.birthday) {
    dateParts.value = form.birthday.split('-')
  }
  user.profile = { ...(user.profile || {}), ...data, token: user.token }
}

async function submit() {
  saving.value = true
  try {
    await http.put('/me', {
      nickname: form.nickname.trim(),
      mobile: form.mobile.trim(),
      gender: form.gender || null,
      birthday: form.birthday || null,
      address: form.address.trim(),
      tags: form.tags
    })
    toast('已保存')
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
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 12px;
}
.hint {
  color: #969799;
  font-size: 12px;
}
.action {
  margin: 24px 16px;
}
</style>
