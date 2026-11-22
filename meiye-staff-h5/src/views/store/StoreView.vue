<template>
  <div>
    <van-nav-bar title="门店" />
    <van-cell-group inset>
      <van-cell v-if="user.hasPermission('store:manage')" title="门店信息" label="维护负责的门店" />
      <van-cell v-if="user.hasPermission('inspection:report')" title="巡店上报" label="填写巡店记录和位置" />
      <van-cell v-if="user.hasPermission('performance:store')" title="门店业绩" label="按门店和时间查看" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'

const user = useUserStore()
</script>
