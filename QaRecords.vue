<template>
  <div>
    <el-card shadow="never" style="margin-bottom:14px">
      <el-space wrap>
        <span>时间范围：</span>
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
        <span>命中分类：</span>
        <el-select v-model="category" clearable placeholder="全部" style="width:150px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
        </el-select>
        <el-button type="primary" @click="reload">查询</el-button>
      </el-space>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="rows" stripe>
        <el-table-column label="时间" width="160" :formatter="r => fmt(r.createdAt)" />
        <el-table-column prop="studentId" label="学生ID" width="80" />
        <el-table-column prop="content" label="提问内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="categoryCode" label="命中分类" width="110">
          <template #default="{ row }">{{ row.categoryCode || '未命中' }}</template>
        </el-table-column>
        <el-table-column prop="emotionTag" label="情绪" width="80" />
        <el-table-column label="风险" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.riskFlag" type="danger" size="small">风险</el-tag>
            <span v-else style="color:#909399">—</span>
          </template>
        </el-table-column>
        <el-table-column label="会话状态" width="100">
          <template #default="{ row }">
            {{ row.sessionStatus === 'ESCALATED' ? '已转人工' : row.sessionStatus === 'AI' ? 'AI 陪伴' : '已结束' }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:12px; justify-content:flex-end" layout="total, prev, pager, next"
                     :total="total" :page-size="20" v-model:current-page="page" @current-change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../utils/http'

const rows = ref([])
const categories = ref([])
const range = ref([])
const category = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(true)

function fmt(t) { return String(t || '').replace('T', ' ').slice(0, 16) }

async function load() {
  loading.value = true
  try {
    const data = await http.get('/admin/qa-records', {
      params: {
        page: page.value - 1, size: 20,
        start: range.value?.[0] || undefined,
        end: range.value?.[1] || undefined,
        category: category.value || undefined
      }
    })
    rows.value = data.content
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

onMounted(async () => {
  categories.value = await http.get('/admin/kb/categories')
  load()
})
</script>
