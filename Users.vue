<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="role" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="STUDENT">学生</el-radio-button>
        <el-radio-button value="COUNSELOR">咨询师</el-radio-button>
        <el-radio-button value="ADMIN">管理员</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="counselorVisible = true">＋ 创建咨询师账号</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="users" stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'COUNSELOR' ? 'warning' : 'primary'" size="small">
              {{ roleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="扩展信息" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.role === 'STUDENT' ? `${row.school || ''} ${row.grade || ''}${row.className || ''}` : row.specialty || row.title || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="权限" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.permissions || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status === 'ACTIVE' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'ACTIVE' && row.role !== 'ADMIN'" link type="danger" @click="toggle(row, 'DISABLED')">停用</el-button>
            <el-button v-else-if="row.role !== 'ADMIN'" link type="success" @click="toggle(row, 'ACTIVE')">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建咨询师 -->
    <el-dialog v-model="counselorVisible" title="创建咨询师账号" width="520px">
      <el-form :model="counselor" label-width="100px">
        <el-form-item label="登录账号" required><el-input v-model="counselor.username" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="counselor.password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="counselor.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="counselor.phone" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="counselor.title" placeholder="如：国家二级心理咨询师" /></el-form-item>
        <el-form-item label="执业证号"><el-input v-model="counselor.licenseNo" /></el-form-item>
        <el-form-item label="擅长领域"><el-input v-model="counselor.specialty" placeholder="逗号分隔，如：考试焦虑,情绪管理" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="counselor.intro" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="counselorPerms">
            <el-checkbox value="case:view">查看案例</el-checkbox>
            <el-checkbox value="case:edit">编辑案例</el-checkbox>
            <el-checkbox value="session:view">查看会话</el-checkbox>
            <el-checkbox value="stats:view">查看统计</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="counselorVisible = false">取消</el-button>
        <el-button type="primary" @click="createCounselor">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑权限/重置密码 -->
    <el-dialog v-model="editVisible" title="编辑账号" width="440px">
      <el-form label-width="100px">
        <el-form-item label="账号"><el-input :value="editRow?.username" disabled /></el-form-item>
        <el-form-item label="权限串"><el-input v-model="editPerms" placeholder="逗号分隔，如 case:view,session:view" /></el-form-item>
        <el-form-item label="重置密码"><el-input v-model="editPwd" show-password placeholder="留空则不修改" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const users = ref([])
const role = ref('')
const loading = ref(true)
const counselorVisible = ref(false)
const editVisible = ref(false)
const editRow = ref(null)
const editPerms = ref('')
const editPwd = ref('')

const counselor = reactive({ username: '', password: '', nickname: '', phone: '', title: '', licenseNo: '', specialty: '', intro: '' })
const counselorPerms = ref(['case:view', 'case:edit', 'session:view'])

function roleText(r) { return r === 'ADMIN' ? '管理员' : r === 'COUNSELOR' ? '咨询师' : '学生' }

async function load() {
  loading.value = true
  try {
    users.value = await http.get('/admin/users', { params: { role: role.value || undefined } })
  } finally {
    loading.value = false
  }
}

async function createCounselor() {
  if (!counselor.username || !counselor.password) return ElMessage.warning('账号与密码必填')
  await http.post('/admin/counselors', { ...counselor, permissions: counselorPerms.value.join(',') })
  ElMessage.success('咨询师账号已创建')
  counselorVisible.value = false
  load()
}

function openEdit(row) {
  editRow.value = row
  editPerms.value = row.permissions || ''
  editPwd.value = ''
  editVisible.value = true
}

async function saveEdit() {
  await http.put(`/admin/users/${editRow.value.id}`, { permissions: editPerms.value, password: editPwd.value || undefined })
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function toggle(row, status) {
  await http.put(`/admin/users/${row.id}`, { status })
  ElMessage.success(status === 'ACTIVE' ? '已启用' : '已停用')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
</style>
