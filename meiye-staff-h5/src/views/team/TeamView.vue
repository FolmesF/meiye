<template>
  <div>
    <van-nav-bar title="团队" />
    <van-cell-group inset>
      <van-cell v-if="user.hasPermission('employee:monitor')" title="工作状态" label="在岗、打卡和最近报岗" />
      <van-cell v-if="user.hasPermission('work:annotate')" title="工作批注" />
      <van-cell v-if="user.hasPermission('performance:team')" title="团队业绩" label="按人员、门店、时间聚合" />
      <van-cell v-if="user.hasPermission('employee:manage')" title="员工建档" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'

const user = useUserStore()
</script>
