<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="banner">
        <div class="title">🌈 青少年心理辅导咨询系统</div>
        <div class="sub">学生提问 · 双路径应答 · 咨询师介入</div>
      </div>
      <el-tabs v-model="tab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" @keyup.enter="doLogin">
            <el-form-item><el-input v-model="loginForm.username" placeholder="账号" size="large"><template #prefix><el-icon><User /></el-icon></template></el-input></el-form-item>
            <el-form-item><el-input v-model="loginForm.password" type="password" show-password placeholder="密码" size="large"><template #prefix><el-icon><Lock /></el-icon></template></el-input></el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doLogin">登 录</el-button>
            <div class="demo-tip">演示账号：student01 / counselor01（123456）、admin（admin123）</div>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册（学生）" name="register">
          <el-form :model="regForm" label-width="70px">
            <el-form-item label="账号"><el-input v-model="regForm.username" placeholder="6-20 位，登录使用" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="regForm.password" type="password" show-password placeholder="至少 6 位" /></el-form-item>
            <el-form-item label="昵称"><el-input v-model="regForm.nickname" placeholder="想被怎么称呼" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="regForm.phone" /></el-form-item>
            <el-form-item label="学校"><el-input v-model="regForm.school" /></el-form-item>
            <el-form-item label="年级"><el-input v-model="regForm.grade" placeholder="如：高二" /></el-form-item>
            <el-form-item label="班级"><el-input v-model="regForm.className" placeholder="如：3班" /></el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../utils/http'
import { setAuth, homeByRole } from '../utils/auth'

const router = useRouter()
const tab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', nickname: '', phone: '', school: '', grade: '', className: '' })

async function afterAuth(data) {
  setAuth(data.token, data.user)
  ElMessage.success('欢迎回来！')
  router.push(homeByRole(data.user.role))
}

async function doLogin() {
  if (!loginForm.username || !loginForm.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    afterAuth(await http.post('/auth/login', loginForm))
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!regForm.username || !regForm.password) return ElMessage.warning('请输入账号和密码')
  if (regForm.password.length < 6) return ElMessage.warning('密码至少 6 位')
  loading.value = true
  try {
    afterAuth(await http.post('/auth/register', regForm))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #a8c8f0 0%, #d8bdf2 50%, #fbd3a1 100%);
}
.login-card {
  width: 440px;
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px 32px;
  box-shadow: 0 12px 40px rgba(31, 45, 61, .18);
}
.banner { text-align: center; margin-bottom: 18px; }
.title { font-size: 20px; font-weight: 700; color: #1f2d3d; }
.sub { color: #909399; font-size: 13px; margin-top: 6px; }
.demo-tip { margin-top: 12px; font-size: 12px; color: #909399; text-align: center; }
</style>
