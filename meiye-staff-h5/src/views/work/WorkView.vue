<template>
  <div class="page">
    <section class="hero">
      <div class="name">{{ user.profile?.name || '掌络' }}</div>
      <div class="role">{{ user.roleName }}</div>
    </section>
    <van-cell-group v-if="roleMenus.length" inset :title="`${user.roleName}功能`">
      <van-cell
        v-for="item in roleMenus"
        :key="item.path"
        :title="item.title"
        :label="item.desc"
        is-link
        :to="item.path"
      />
    </van-cell-group>
    <van-cell-group v-if="commonMenus.length" inset title="通用功能">
      <van-cell
        v-for="item in commonMenus"
        :key="item.path"
        :title="item.title"
        :label="item.desc"
        is-link
        :to="item.path"
      />
    </van-cell-group>
    <van-empty v-if="!roleMenus.length && !commonMenus.length" description="当前账号没有可用功能" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../../stores/user'
import { commonEntries, roleEntries, visibleEntries } from '../../router/menus'

const user = useUserStore()
const roleMenus = computed(() => visibleEntries(roleEntries[user.role] || [], user.hasPermission))
const commonMenus = computed(() => visibleEntries(commonEntries, user.hasPermission))
</script>

<style scoped>
.hero {
  margin: 16px;
  padding: 20px;
  border-radius: 12px;
  color: #fff;
  background: #1989fa;
}
.name {
  font-size: 22px;
  font-weight: 600;
}
.role {
  margin-top: 6px;
  font-size: 14px;
}
</style>
