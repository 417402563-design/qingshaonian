<template>
  <div>
    <el-card shadow="never" style="margin-bottom:14px">
      <el-space wrap>
        <span>统计区间：</span>
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
        <el-button type="primary" :loading="loading" @click="load">分析</el-button>
        <el-statistic title="区间提问总量" :value="data.totalQuestions || 0" style="margin-left:24px" />
        <el-statistic title="风险提问" :value="data.riskCount || 0" style="margin-left:24px" />
      </el-space>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never">
          <div class="card-title">📈 每日问答量趋势</div>
          <div ref="trendRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="card-title">🧩 问题分类分布</div>
          <div ref="catRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="card-title">🎭 情绪标签分布</div>
          <div ref="emoRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="card-title">🔥 关键词热词榜</div>
          <div class="hot-rank">
            <div v-for="(w, i) in (data.hotWords || []).slice(0, 12)" :key="w.name" class="hot-item">
              <span class="rank" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <span class="word">{{ w.name }}</span>
              <el-progress :percentage="pct(w.value)" :show-text="false" :stroke-width="10" style="flex:1" />
              <span class="count">{{ w.value }}</span>
            </div>
            <div v-if="!(data.hotWords || []).length" class="empty-tip">暂无数据，先让学生们来聊聊吧</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="never">
          <div class="card-title">☁️ 心理热词词云（字号 = 提及频次）</div>
          <div ref="cloudRef" style="height:420px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import 'echarts-wordcloud'
import http from '../../utils/http'

const range = ref([])
const data = ref({})
const loading = ref(false)
const trendRef = ref(null)
const catRef = ref(null)
const emoRef = ref(null)
const cloudRef = ref(null)
const charts = []

function pct(v) {
  const max = (data.value.hotWords || [])[0]?.value || 1
  return Math.round((v / max) * 100)
}

function init(el) {
  let c = echarts.getInstanceByDom(el)
  if (!c) {
    c = echarts.init(el)
    charts.push(c)
  }
  return c
}

async function load() {
  loading.value = true
  try {
    data.value = await http.get('/admin/analytics', {
      params: { start: range.value?.[0] || undefined, end: range.value?.[1] || undefined }
    })
    render()
  } finally {
    loading.value = false
  }
}

function render() {
  const d = data.value
  init(trendRef.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: (d.trend || []).map(t => t.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line', smooth: true, areaStyle: { opacity: .15 },
      itemStyle: { color: '#409eff' },
      data: (d.trend || []).map(t => t.count)
    }]
  })

  init(catRef.value).setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['35%', '65%'], label: { formatter: '{b}\n{c}' },
      data: (d.categoryDist || []).map(x => ({ name: x.name, value: x.value }))
    }]
  })

  init(emoRef.value).setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: '60%', label: { formatter: '{b} {c}' },
      data: (d.emotionDist || []).map(x => ({ name: x.name, value: x.value }))
    }]
  })

  init(cloudRef.value).setOption({
    tooltip: { formatter: '{b}：{c} 次' },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      sizeRange: [16, 72],
      rotationRange: [-45, 45],
      gridSize: 8,
      textStyle: { color: () => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#a78bfa', '#f472b6'][Math.floor(Math.random() * 7)] },
      data: (d.hotWords || []).map(w => ({ name: w.name, value: w.value }))
    }]
  })
}

onMounted(load)
window.addEventListener('resize', () => charts.forEach(c => c.resize()))
</script>

<style scoped>
.hot-rank { max-height: 300px; overflow-y: auto; }
.hot-item { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.rank { width: 22px; height: 22px; border-radius: 50%; background: #eef2f7; color: #909399; text-align: center; line-height: 22px; font-size: 12px; }
.rank.top { background: #f56c6c; color: #fff; }
.word { width: 60px; font-weight: 600; }
.count { color: #909399; font-size: 12px; width: 36px; text-align: right; }
</style>
