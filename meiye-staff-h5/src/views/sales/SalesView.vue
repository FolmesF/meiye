<template>
  <div>
    <van-nav-bar title="客户" />
    <van-cell-group inset>
      <van-cell v-if="user.hasPermission('visit-template:manage')" title="陌拜模板" />
      <van-cell v-if="user.hasPermission('prospect:write')" title="陌拜客户" />
      <van-cell v-if="user.hasPermission('followup:write')" title="跟进记录" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'

const user = useUserStore()
</script>
