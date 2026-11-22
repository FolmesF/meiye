<template>
  <el-card>
    <template #header>{{ route.meta.title }}</template>
    <p>接口：{{ route.meta.api }}</p>
    <p class="tip">请求会自动带上 Bearer Token，并按统一返回的 code 处理结果。</p>
  </el-card>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.tip {
  color: #667085;
}
</style>
