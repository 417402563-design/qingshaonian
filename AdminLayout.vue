<template>
  <el-container style="min-height: 100vh">
    <el-aside width="230px" class="aside">
      <div class="logo">⚙️ 系统管理端</div>
      <el-menu :default-active="$route.path" router background-color="#141c28" text-color="#9fb0c3" active-text-color="#ffd04b">
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon>数据统计看板</el-menu-item>
        <el-menu-item index="/admin/analytics"><el-icon><TrendCharts /></el-icon>问答可视化·热词</el-menu-item>
        <el-menu-item index="/admin/qa"><el-icon><Tickets /></el-icon>问答记录</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon>账号与权限</el-menu-item>
        <el-menu-item index="/admin/knowledge"><el-icon><Collection /></el-icon>知识库运营</el-menu-item>
        <el-menu-item index="/admin/content"><el-icon><EditPen /></el-icon>内容管理</el-menu-item>
        <el-menu-item index="/admin/configs"><el-icon><Setting /></el-icon>系统配置</el-menu-item>
        <el-menu-item index="/admin/monitor"><el-icon><Monitor /></el-icon>服务状态监控</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>管理员：{{ nickname }}</span>
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
.aside { background: #141c28; }
.logo { color: #fff; font-weight: 700; padding: 18px 16px; font-size: 16px; }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
</style>
