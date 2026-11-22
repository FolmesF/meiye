<template>
  <van-nav-bar :title="title" :left-arrow="leftArrow" fixed placeholder safe-area-inset-top @click-left="onBack" />
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '' },
  leftArrow: { type: Boolean, default: false }
})

const router = useRouter()

function onBack() {
  if (!props.leftArrow) return
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.replace('/home')
}
</script>
