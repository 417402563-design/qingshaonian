<template>
  <div class="page">
    <div class="card-title" style="font-size:20px">🧭 自我探索</div>
    <div class="sub">做一次心理小测评，像照镜子一样看见最近的自己。答案没有对错，凭第一感觉选就好。</div>

    <el-empty v-if="!loading && !surveys.length" description="暂无问卷" />

    <el-card v-for="s in surveys" :key="s.id" class="survey-card" shadow="never" v-loading="loading">
      <div class="survey-title">{{ s.title }}</div>
      <div class="survey-desc">{{ s.description }}</div>
      <el-form label-position="top">
        <div v-for="(q, qi) in s.questions" :key="qi" class="question">
          <div class="q-text">{{ qi + 1 }}. {{ q.title }}</div>
          <el-radio-group v-model="answers[s.id][qi]">
            <el-radio v-for="(opt, oi) in q.options" :key="oi" :value="oi" border size="small">{{ opt.label }}</el-radio>
          </el-radio-group>
        </div>
      </el-form>
      <el-button type="primary" round :loading="submitting" @click="submit(s)">提交答卷</el-button>

      <el-alert v-if="results[s.id]" class="result" type="success" :closable="false" show-icon>
        <template #title>{{ s.title }}：得分 {{ results[s.id].score }}，{{ results[s.id].level }}</template>
        <div>{{ results[s.id].advice }}</div>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const surveys = ref([])
const answers = reactive({})
const results = reactive({})
const loading = ref(true)
const submitting = ref(false)

async function load() {
  try {
    surveys.value = await http.get('/student/surveys')
    surveys.value.forEach(s => { answers[s.id] = new Array(s.questions.length).fill(null) })
  } finally {
    loading.value = false
  }
}

async function submit(s) {
  if (answers[s.id].some(a => a === null || a === undefined)) {
    return ElMessage.warning('还有题目没有作答哦')
  }
  submitting.value = true
  try {
    results[s.id] = await http.post(`/student/surveys/${s.id}/submit`, { answers: answers[s.id] })
  } finally {
    submitting.value = false
  }
}

load()
</script>

<style scoped>
.sub { color: #909399; font-size: 13px; margin: 6px 0 18px; }
.survey-card { margin-bottom: 20px; max-width: 860px; }
.survey-title { font-size: 17px; font-weight: 700; }
.survey-desc { color: #909399; font-size: 13px; margin: 6px 0 14px; }
.question { margin-bottom: 16px; }
.q-text { font-weight: 600; margin-bottom: 8px; }
.result { margin-top: 16px; white-space: pre-wrap; }
</style>
