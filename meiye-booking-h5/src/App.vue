<template>
  <van-config-provider :theme-vars="themeVars">
    <router-view />
  </van-config-provider>
</template>

<script setup>
import { themeVars } from './theme'
</script>
