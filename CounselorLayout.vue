<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">🧑‍⚕️ 咨询师工作台</div>
      <el-menu :default-active="$route.path" router background-color="#1f2d3d" text-color="#a7b6c5" active-text-color="#ffd04b">
        <el-menu-item index="/counselor/workbench"><el-icon><Odometer /></el-icon>工作台总览</el-menu-item>
        <el-menu-item index="/counselor/sessions"><el-icon><Warning /></el-icon>待介入会话</el-menu-item>
        <el-menu-item index="/counselor/cases"><el-icon><Notebook /></el-icon>案例干预</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>心理咨询师：{{ nickname }}</span>
        <el-button link type="danger" @click="logout">退出登录</el-button>
      </el-header>
      <el-main style="background:#f5f7fa"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUser, clearAuth } from '../utils/auth'

const router = useRouter()
const nickname = computed(() => getUser().nickname || getUser().username || '')

function logout() {
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.aside { background: #1f2d3d; }
.logo { color: #fff; font-weight: 700; padding: 18px 16px; font-size: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
</style>
