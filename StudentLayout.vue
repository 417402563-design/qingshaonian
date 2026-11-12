<template>
  <div>
    <el-menu mode="horizontal" :default-active="$route.path" router :ellipsis="false" class="top-menu">
      <el-menu-item disabled class="brand">🌈 青少年心理小屋</el-menu-item>
      <el-menu-item index="/student/home">首页</el-menu-item>
      <el-menu-item index="/student/chat">智能咨询</el-menu-item>
      <el-menu-item index="/student/stories">榜样故事</el-menu-item>
      <el-menu-item index="/student/exam">高考冲锋</el-menu-item>
      <el-menu-item index="/student/articles">成长驿站</el-menu-item>
      <el-menu-item index="/student/surveys">自我探索</el-menu-item>
      <div style="flex:1"></div>
      <el-menu-item index="/student/profile">
        <el-icon><User /></el-icon>&nbsp;{{ nickname }}
      </el-menu-item>
      <el-menu-item class="logout-item" @click="logout">退出</el-menu-item>
    </el-menu>
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUser, clearAuth } from '../utils/auth'

const router = useRouter()
const nickname = computed(() => getUser().nickname || getUser().username || '同学')

function logout() {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.top-menu { background: #fff; box-shadow: 0 1px 4px rgba(0, 0, 0, .06); }
.brand { font-weight: 700; font-size: 16px; }
.logout-item { cursor: pointer; color: #f56c6c; }
</style>
