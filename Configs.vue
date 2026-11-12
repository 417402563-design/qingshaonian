<template>
  <div>
    <div class="toolbar">
      <span class="tip">运营参数（欢迎语、热线号码等）保存后即时生效；DeepSeek API Key 等安全参数请在后端 application.properties 中维护。</span>
      <el-button type="primary" @click="openEdit()">＋ 新增配置</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column prop="configKey" label="配置项" width="220" />
        <el-table-column prop="configValue" label="配置值" min-width="260" show-overflow-tooltip />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑配置' : '新增配置'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="配置项" required><el-input v-model="form.configKey" placeholder="如 consult.welcome" /></el-form-item>
        <el-form-item label="配置值"><el-input v-model="form.configValue" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(true)
const visible = ref(false)
const form = reactive({ id: null, configKey: '', configValue: '', description: '' })

async function load() {
  loading.value = true
  try {
    list.value = await http.get('/admin/configs')
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.assign(form, row ? { ...row } : { id: null, configKey: '', configValue: '', description: '' })
  visible.value = true
}

async function save() {
  if (!form.configKey) return ElMessage.warning('配置项必填')
  await http.post('/admin/configs', { ...form })
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除配置「${row.configKey}」？`, '提示', { type: 'warning' })
  await http.delete(`/admin/configs/${row.id}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; max-width: 70%; }
</style>
