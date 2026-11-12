<template>
  <div>
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
              title="以下会话由智能咨询的风险识别自动标记，危机(红)优先处理；可查看详情或一键立卷进入干预流程。" />
    <el-card shadow="never" v-loading="loading">
      <el-table :data="rows" stripe>
        <el-table-column label="风险" width="90">
          <template #default="{ row }">
            <el-tag :type="row.session.riskLevel === 'HIGH' ? 'danger' : 'warning'" size="small" effect="dark">
              {{ row.session.riskLevel === 'HIGH' ? '危机' : '关注' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学生" width="150">
          <template #default="{ row }">
            {{ row.studentName || row.studentId }}
            <div style="font-size:11px;color:#909399">{{ row.grade }} {{ row.className }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="session.title" label="会话主题" width="180" show-overflow-tooltip />
        <el-table-column label="摘录（最后提问）" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ ellipsis(row.excerpt) }}</template>
        </el-table-column>
        <el-table-column prop="session.emotionTag" label="情绪" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ row.session.status === 'ESCALATED' ? '已转人工' : 'AI 陪伴中' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="150" :formatter="r => fmt(r.session.createdAt)" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="view(row.session.id)">对话</el-button>
            <el-button link type="success" @click="createFrom(row.session.id)">立卷干预</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !rows.length" description="太好了，当前没有风险会话 🌤" />
    </el-card>

    <el-dialog v-model="dialog" title="风险对话回放" width="620px" top="6vh">
      <div style="max-height:60vh;overflow-y:auto">
        <div v-for="m in msgs" :key="m.id" class="line" :class="m.role">
          <span class="who">{{ m.role === 'user' ? '学生' : 'AI 伙伴' }}：</span>{{ m.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const rows = ref([])
const msgs = ref([])
const dialog = ref(false)
const loading = ref(true)

function fmt(t) { return String(t || '').replace('T', ' ').slice(0, 16) }
function ellipsis(t) { return (t || '').length > 60 ? (t || '').slice(0, 60) + '…' : t }

async function load() {
  rows.value = await http.get('/counselor/sessions/risk')
  loading.value = false
}

async function view(sessionId) {
  msgs.value = await http.get(`/counselor/sessions/${sessionId}/messages`)
  dialog.value = true
}

async function createFrom(sessionId) {
  await http.post(`/counselor/cases/from-session/${sessionId}`)
  ElMessage.success('已立卷，可在「案例干预」中跟进')
  dialog.value = false
  router.push('/counselor/cases')
}

onMounted(load)
</script>

<style scoped>
.line { margin-bottom: 10px; line-height: 1.7; white-space: pre-wrap; }
.line.user .who { color: #e6a23c; font-weight: 600; }
.line.assistant .who { color: #409eff; font-weight: 600; }
</style>
