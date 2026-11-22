<template>
  <el-result icon="info" title="页面不存在" sub-title="请从菜单重新进入">
    <template #extra>
      <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
    </template>
  </el-result>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>
