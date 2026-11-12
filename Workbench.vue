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

    <el-card shadow="never" style="margin-top:16px">
      <div class="card-title">📌 我最近的案例</div>
      <el-table :data="recentCases" stripe v-loading="loading">
        <el-table-column prop="summary" label="案例摘要" min-width="240" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="row.level === '危机' ? 'danger' : row.level === '重点' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" :formatter="r => fmt(r.createdAt)" />
        <el-table-column label="操作" width="90">
          <template #default>
            <el-button link type="primary" @click="$router.push('/counselor/cases')">进入案例干预</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !recentCases.length" description="暂无案例，去「待介入会话」看看有没有需要帮助的同学吧" />
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <div class="card-title">💡 工作建议</div>
      <ul class="tips">
        <li>危机等级的会话请在 24 小时内完成首次干预记录；</li>
        <li>干预方案遵循「建立关系 → 稳定化 → 问题工作 → 巩固结束」四阶段；</li>
        <li>涉及自伤他伤风险的案例，务必同步学校心理危机干预流程。</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../utils/http'

const data = ref({})
const loading = ref(true)
const recentCases = computed(() => data.value.recentCases || [])

const cards = computed(() => [
  { label: '我的案例总数', value: data.value.myCaseTotal ?? 0, icon: '📚', color: '#409eff' },
  { label: '进行中案例', value: data.value.myCaseActive ?? 0, icon: '🚀', color: '#67c23a' },
  { label: '平台风险会话', value: data.value.riskSessionTotal ?? 0, icon: '⚠️', color: '#e6a23c' },
  { label: '待我介入(已转人工)', value: data.value.escalatedPending ?? 0, icon: '🆘', color: '#f56c6c' }
])

function fmt(t) { return String(t || '').replace('T', ' ').slice(0, 16) }
function statusText(s) { return s === 'OPEN' ? '待领取' : s === 'IN_PROGRESS' ? '干预中' : '已结案' }

onMounted(async () => {
  data.value = await http.get('/counselor/dashboard')
  loading.value = false
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 30px; font-weight: 700; }
.stat-label { color: #909399; margin-top: 6px; font-size: 13px; }
.tips li { margin: 8px 0 8px 20px; color: #606266; line-height: 1.7; }
</style>
