<template>
  <el-result icon="warning" title="无权限" sub-title="当前账号的菜单里没有这个功能">
    <template #extra>
      <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
    </template>
  </el-result>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>
