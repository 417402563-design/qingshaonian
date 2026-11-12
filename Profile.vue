<template>
  <div class="page">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card shadow="never">
          <div class="card-title">👤 我的资料</div>
          <el-form :model="form" label-width="90px">
            <el-form-item label="账号"><el-input :value="me.username" disabled /></el-form-item>
            <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
            <el-form-item label="年级"><el-input v-model="form.grade" /></el-form-item>
            <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
            <el-form-item label="心理档案"><el-input :value="me.psychProfileNo" disabled /></el-form-item>
            <el-button type="primary" @click="save">保存资料</el-button>
          </el-form>
          <el-divider />
          <div class="card-title">🔒 修改密码</div>
          <el-form :model="pwd" label-width="90px">
            <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
            <el-button type="warning" @click="changePwd">修改密码</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <div class="card-title">🗂 历史咨询记录</div>
          <el-table :data="sessions" stripe v-loading="loading">
            <el-table-column prop="title" label="会话主题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="categoryCode" label="命中分类" width="110">
              <template #default="{ row }">{{ row.categoryCode || '—' }}</template>
            </el-table-column>
            <el-table-column label="风险" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.riskLevel === 'HIGH'" type="danger" size="small">高风险</el-tag>
                <el-tag v-else-if="row.riskLevel === 'MEDIUM'" type="warning" size="small">关注</el-tag>
                <el-tag v-else type="info" size="small">无</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                {{ row.status === 'ESCALATED' ? '已转人工' : row.status === 'AI' ? 'AI 陪伴中' : '已结束' }}
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="时间" width="150" :formatter="r => fmt(r.updatedAt)" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click="view(row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialog" title="对话回放" width="640px" top="6vh">
      <div style="max-height:60vh; overflow-y:auto">
        <div v-for="m in msgs" :key="m.id" class="line" :class="m.role">
          <span class="who">{{ m.role === 'user' ? '我' : '倾听伙伴' }}：</span>{{ m.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'
import { setAuth, getUser } from '../../utils/auth'

const me = ref(getUser())
const form = reactive({ nickname: '', phone: '', school: '', grade: '', className: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const sessions = ref([])
const msgs = ref([])
const dialog = ref(false)
const loading = ref(true)

function fmt(t) { return String(t || '').replace('T', ' ').slice(0, 16) }

async function loadMe() {
  me.value = await http.get('/auth/me')
  Object.assign(form, {
    nickname: me.value.nickname, phone: me.value.phone,
    school: me.value.school, grade: me.value.grade, className: me.value.className
  })
}

async function save() {
  await http.put('/auth/profile', form)
  setAuth(localStorage.getItem('qs_token'), { ...me.value, ...form })
  ElMessage.success('资料已更新')
}

async function changePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) return ElMessage.warning('请填写完整')
  await http.put('/auth/password', pwd)
  ElMessage.success('密码修改成功')
  pwd.oldPassword = pwd.newPassword = ''
}

async function loadSessions() {
  sessions.value = await http.get('/student/chat/sessions')
  loading.value = false
}

async function view(id) {
  msgs.value = await http.get(`/student/chat/sessions/${id}/messages`)
  dialog.value = true
}

onMounted(() => { loadMe(); loadSessions() })
</script>

<style scoped>
.line { margin-bottom: 10px; line-height: 1.7; white-space: pre-wrap; }
.line.user .who { color: #4f7cff; font-weight: 600; }
.line.assistant .who { color: #67c23a; font-weight: 600; }
</style>
