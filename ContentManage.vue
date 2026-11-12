<template>
  <div>
    <el-radio-group v-model="type" @change="reload" style="margin-bottom:14px">
      <el-radio-button value="stories">🌟 榜样故事</el-radio-button>
      <el-radio-button value="exam">🎯 高考冲锋专栏</el-radio-button>
      <el-radio-button value="articles">🌱 成长驿站科普</el-radio-button>
    </el-radio-group>

    <div class="toolbar">
      <span class="tip">{{ tips[type] }}</span>
      <el-button type="primary" @click="openEdit()">＋ 新增{{ typeName }}</el-button>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column :prop="titleKey" :label="typeName" min-width="220" show-overflow-tooltip />
        <el-table-column v-if="type === 'stories'" prop="tag" label="标签" width="110" />
        <el-table-column v-if="type === 'exam'" prop="type" label="专栏类型" width="110" />
        <el-table-column v-if="type === 'articles'" prop="category" label="科普分类" width="110" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">{{ row.status === 'PUBLISHED' ? '已发布' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:12px; justify-content:flex-end" layout="prev, pager, next"
                     :total="total" :page-size="10" v-model:current-page="page" @current-change="load" />
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑' + typeName : '新增' + typeName" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item :label="typeName" required><el-input v-model="form[titleKey]" /></el-form-item>
        <el-form-item v-if="type === 'stories'" label="人物标签"><el-input v-model="form.tag" placeholder="如：拼搏励志" /></el-form-item>
        <el-form-item v-if="type === 'exam'" label="专栏类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="冲刺心理" value="冲刺心理" /><el-option label="减压技巧" value="减压技巧" /><el-option label="备考策略" value="备考策略" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="type === 'articles'" label="科普分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="情绪" value="情绪" /><el-option label="人际" value="人际" />
            <el-option label="家庭" value="家庭" /><el-option label="成长" value="成长" />
          </el-select>
        </el-form-item>
        <el-form-item label="一句话摘要"><el-input v-model="form.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="正文" required><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item v-if="type === 'stories'" label="精选推荐"><el-switch v-model="form.featured" /></el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status"><el-radio value="PUBLISHED">发布</el-radio><el-radio value="OFFLINE">下架</el-radio></el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const type = ref('stories')
const list = ref([])
const page = ref(1)
const total = ref(0)
const loading = ref(true)
const visible = ref(false)
const form = reactive({})

const titleKey = computed(() => (type.value === 'stories' ? 'name' : 'title'))
const typeName = computed(() => ({ stories: '榜样人物', exam: '专栏内容', articles: '科普文章' }[type.value]))
const tips = {
  stories: '榜样故事会展示在学生的「榜样故事栏」，建议突出「低谷-转折-方法」的叙事结构。',
  exam: '高考冲锋专栏按「冲刺心理 / 减压技巧 / 备考策略」三类运营。',
  articles: '成长驿站科普按「情绪 / 人际 / 家庭 / 成长」四类运营。'
}

async function load() {
  loading.value = true
  try {
    const data = await http.get(`/content/${type.value}`, { params: { page: page.value - 1, size: 10 } })
    list.value = data.content
    total.value = data.totalElements
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function openEdit(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row ? { ...row } : { status: 'PUBLISHED', featured: false })
  visible.value = true
}

async function save() {
  if (!form[titleKey] || !form.content) return ElMessage.warning('标题与正文必填')
  await http.post(`/admin/${type.value}`, { ...form })
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除「${row[titleKey]}」？`, '提示', { type: 'warning' })
  await http.delete(`/admin/${type.value}/${row.id}`)
  ElMessage.success('已删除')
  load()
}

load()
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; }
</style>
