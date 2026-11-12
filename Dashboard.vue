<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: c.color }">{{ c.value }}</div>
          <div class="stat-label">{{ c.icon }} {{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-title">案例干预状态分布</div>
          <div ref="caseChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-title">🚨 风险与运营提醒</div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="高危会话（需立即介入）">
              <el-tag :type="ov.highRiskSessions ? 'danger' : 'success'">{{ ov.highRiskSessions || 0 }} 个</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="待领取案例">{{ ov.caseOpen || 0 }} 个，建议咨询师尽快认领</el-descriptions-item>
            <el-descriptions-item label="知识库条目">{{ ov.knowledgeCount || 0 }} 条</el-descriptions-item>
            <el-descriptions-item label="大模型状态">
              <el-tag :type="ov.modelConfigured ? 'success' : 'warning'">{{ ov.modelConfigured ? 'DeepSeek 已接入' : '未配置 API Key（知识库兜底）' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:14px">
            <el-button type="primary" plain @click="$router.push('/admin/analytics')">查看问答可视化与热词分析</el-button>
            <el-button plain @click="$router.push('/admin/monitor')">服务状态监控</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import http from '../../utils/http'

const ov = ref({})
const caseChartRef = ref(null)

const cards = computed(() => [
  { label: '注册学生', value: ov.value.studentCount ?? 0, icon: '🎓', color: '#409eff' },
  { label: '咨询师', value: ov.value.counselorCount ?? 0, icon: '🧑‍⚕️', color: '#67c23a' },
  { label: '咨询会话', value: ov.value.sessionCount ?? 0, icon: '💬', color: '#e6a23c' },
  { label: '累计提问', value: ov.value.questionCount ?? 0, icon: '📮', color: '#f56c6c' }
])

onMounted(async () => {
  const [overview, monitor] = await Promise.all([http.get('/admin/stats/overview'), http.get('/admin/monitor')])
  ov.value = { ...overview, modelConfigured: monitor.model?.configured }
  const chart = echarts.init(caseChartRef.value)
  chart.setOption({
    tooltip: {},
    series: [{
      type: 'pie', radius: ['40%', '68%'], label: { formatter: '{b}: {c}' },
      data: [
        { name: '待领取', value: overview.caseOpen },
        { name: '干预中', value: overview.caseInProgress },
        { name: '已结案', value: overview.caseClosed }
      ],
      color: ['#e6a23c', '#409eff', '#67c23a']
    }]
  })
  window.addEventListener('resize', () => chart.resize())
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 30px; font-weight: 700; }
.stat-label { color: #909399; margin-top: 6px; font-size: 13px; }
</style>
