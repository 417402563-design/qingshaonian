<template>
  <div>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="OPEN">待领取</el-radio-button>
        <el-radio-button value="IN_PROGRESS">干预中</el-radio-button>
        <el-radio-button value="CLOSED">已结案</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="openForm">＋ 新建案例</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="cases" stripe>
        <el-table-column prop="summary" label="案例摘要" min-width="240" show-overflow-tooltip />
        <el-table-column label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.level === '危机' ? 'danger' : row.level === '重点' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="problemCategory" label="问题分类" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="nextFollowUpDate" label="下次跟进" width="110" />
        <el-table-column label="更新时间" width="160" :formatter="r => fmt(r.updatedAt)" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">干预档案</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !cases.length" description="暂无案例" />
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" title="案例干预档案" size="560px">
      <template v-if="detail.case">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="学生">{{ detail.student?.nickname }}（{{ detail.student?.grade }}{{ detail.student?.className }}）</el-descriptions-item>
          <el-descriptions-item label="学校">{{ detail.student?.school || '—' }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detail.case.level }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ detail.case.problemCategory || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detail.case.status) }}</el-descriptions-item>
          <el-descriptions-item label="下次跟进">{{ detail.case.nextFollowUpDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">{{ detail.case.summary }}</el-descriptions-item>
        </el-descriptions>

        <div class="block-title">📝 干预方案</div>
        <el-input v-model="planDraft" type="textarea" :rows="4" placeholder="记录干预目标与方法，保存后生效" />
        <el-button size="small" type="primary" plain style="margin-top:8px" @click="savePlan">保存方案</el-button>

        <div class="block-title">🕘 干预进展</div>
        <div class="progress-box">{{ detail.case.progress || '暂无进展记录' }}</div>
        <el-input v-model="progressDraft" type="textarea" :rows="3" placeholder="本次干预/谈话记录…" />
        <el-space style="margin-top:8px" wrap>
          <el-select v-model="progressStatus" placeholder="状态更新" clearable style="width:130px" size="small">
            <el-option label="干预中" value="IN_PROGRESS" />
            <el-option label="结案" value="CLOSED" />
          </el-select>
          <el-date-picker v-model="followDate" type="date" placeholder="下次跟进日期" value-format="YYYY-MM-DD" size="small" style="width:150px" />
          <el-button size="small" type="success" @click="addProgress">追加进展</el-button>
        </el-space>

        <template v-if="detail.messages?.length">
          <div class="block-title">💬 来源会话对话（AI 阶段）</div>
          <div class="chat-box">
            <div v-for="m in detail.messages" :key="m.id" class="line" :class="m.role">
              <span class="who">{{ m.role === 'user' ? '学生' : 'AI' }}：</span>{{ m.content }}
            </div>
          </div>
        </template>
      </template>
    </el-drawer>

    <!-- 新建案例 -->
    <el-dialog v-model="formVisible" title="新建干预案例" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学生" required>
          <el-select v-model="form.studentId" filterable style="width:100%">
            <el-option v-for="s in students" :key="s.id" :label="`${s.nickname}（${s.grade || ''}${s.className || ''}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="干预等级">
          <el-select v-model="form.level" style="width:100%">
            <el-option label="一般" value="一般" /><el-option label="重点" value="重点" /><el-option label="危机" value="危机" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题分类">
          <el-select v-model="form.problemCategory" clearable style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="情况摘要"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="干预方案"><el-input v-model="form.plan" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="createCase">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const cases = ref([])
const students = ref([])
const categories = ref([])
const detail = ref({})
const status = ref('')
const loading = ref(true)
const drawer = ref(false)
const formVisible = ref(false)
const planDraft = ref('')
const progressDraft = ref('')
const progressStatus = ref('')
const followDate = ref('')

const form = reactive({ studentId: null, level: '一般', problemCategory: '', summary: '', plan: '' })

function fmt(t) { return String(t || '').replace('T', ' ').slice(0, 16) }
function statusText(s) { return s === 'OPEN' ? '待领取' : s === 'IN_PROGRESS' ? '干预中' : '已结案' }

async function load() {
  loading.value = true
  try {
    cases.value = await http.get('/counselor/cases', { params: { status: status.value || undefined } })
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detail.value = await http.get(`/counselor/cases/${id}`)
  planDraft.value = detail.value.case.plan || ''
  progressDraft.value = ''
  progressStatus.value = ''
  followDate.value = ''
  drawer.value = true
}

async function savePlan() {
  await http.post('/counselor/cases', { id: detail.value.case.id, plan: planDraft.value })
  ElMessage.success('方案已保存')
  openDetail(detail.value.case.id)
}

async function addProgress() {
  if (!progressDraft.value.trim()) return ElMessage.warning('请填写进展内容')
  await http.put(`/counselor/cases/${detail.value.case.id}/progress`,
    { content: progressDraft.value, status: progressStatus.value || undefined, nextFollowUpDate: followDate.value || undefined })
  ElMessage.success('进展已记录')
  openDetail(detail.value.case.id)
}

async function openForm() {
  if (!students.value.length) students.value = await http.get('/counselor/students')
  if (!categories.value.length) categories.value = await http.get('/admin/kb/categories').catch(() => http.get('/content/kb/categories'))
  formVisible.value = true
}

async function createCase() {
  if (!form.studentId) return ElMessage.warning('请选择学生')
  await http.post('/counselor/cases', { ...form })
  ElMessage.success('案例已创建')
  formVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
.block-title { font-weight: 700; margin: 18px 0 8px; }
.progress-box { background: #f7f8fa; border-radius: 8px; padding: 12px; min-height: 60px; white-space: pre-wrap; font-size: 13px; color: #606266; margin-bottom: 8px; }
.chat-box { max-height: 260px; overflow-y: auto; background: #fafcff; border-radius: 8px; padding: 12px; }
.line { margin-bottom: 8px; line-height: 1.6; font-size: 13px; white-space: pre-wrap; }
.line.user .who { color: #e6a23c; font-weight: 600; }
.line.assistant .who { color: #409eff; font-weight: 600; }
</style>
