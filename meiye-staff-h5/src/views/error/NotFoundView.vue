<template>
  <div class="page">
    <van-empty description="页面不存在">
      <van-button type="primary" @click="router.replace('/work')">返回工作台</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.page {
  padding-top: 80px;
}
</style>
