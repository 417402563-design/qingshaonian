<template>
  <div>
    <div class="toolbar">
      <span class="tip">实时展示应用、数据库、大模型服务与今日咨询量状态（每 30 秒自动刷新）。</span>
      <el-button type="primary" @click="load">手动刷新</el-button>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="dot" :class="dot(data.appStatus)"></div>
          <div class="name">应用服务</div>
          <div class="val">{{ data.appStatus === 'UP' ? '运行中' : '异常' }}</div>
          <div class="sub">Java {{ data.javaVersion }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="dot" :class="dot(data.dbStatus)"></div>
          <div class="name">MySQL 数据库</div>
          <div class="val">{{ data.dbStatus === 'UP' ? '连接正常' : '连接异常' }}</div>
          <div class="sub">qingshaonian_db</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="dot" :class="data.model?.configured ? 'green' : 'orange'"></div>
          <div class="name">DeepSeek 大模型</div>
          <div class="val">{{ data.model?.configured ? '已接入' : '未配置 Key' }}</div>
          <div class="sub">{{ data.model?.configured ? '知识库检索增强应答' : '自动降级为知识库模板回答' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="status-card">
          <div class="dot blue"></div>
          <div class="name">今日活跃会话</div>
          <div class="val">{{ data.activeSessionsToday ?? 0 }}</div>
          <div class="sub">近 24 小时有互动的会话数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-title">内存使用</div>
          <el-progress :percentage="memPct" :stroke-width="18" text-inside
                       :status="memPct > 85 ? 'exception' : 'success'" />
          <div class="sub" style="margin-top:10px">已用 {{ data.memoryUsedMb }} MB / 上限 {{ data.memoryMaxMb }} MB</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-title">模型调用统计</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="调用总数">{{ data.model?.callCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="失败次数">{{ data.model?.errorCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="平均耗时">{{ data.model?.avgLatencyMs ?? 0 }} ms</el-descriptions-item>
            <el-descriptions-item label="使用模型">{{ data.model?.model || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../../utils/http'

const data = ref({})
const loading = ref(true)
let timer = null

const memPct = computed(() => {
  const d = data.value
  return d.memoryMaxMb ? Math.round((d.memoryUsedMb / d.memoryMaxMb) * 100) : 0
})

function dot(status) { return status === 'UP' ? 'green' : 'red' }

async function load() {
  loading.value = true
  try {
    data.value = await http.get('/admin/monitor')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  timer = setInterval(load, 30000)
})
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; }
.status-card { text-align: center; }
.dot { width: 14px; height: 14px; border-radius: 50%; margin: 0 auto; }
.dot.green { background: #67c23a; box-shadow: 0 0 8px #67c23a; }
.dot.red { background: #f56c6c; box-shadow: 0 0 8px #f56c6c; }
.dot.orange { background: #e6a23c; box-shadow: 0 0 8px #e6a23c; }
.dot.blue { background: #409eff; box-shadow: 0 0 8px #409eff; }
.name { font-weight: 700; margin-top: 10px; }
.val { font-size: 18px; margin-top: 6px; color: #303133; }
.sub { color: #909399; font-size: 12px; margin-top: 6px; }
</style>
