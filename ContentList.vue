<template>
  <div class="page">
    <div class="head">
      <div>
        <span class="card-title" style="font-size:20px">{{ title }}</span>
        <span class="sub">{{ subtitle }}</span>
      </div>
      <el-radio-group v-if="filters.length" v-model="filterValue" @change="reload">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button v-for="f in filters" :key="f" :value="f">{{ f }}</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16" style="margin-top:16px" v-loading="loading">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom:16px">
        <el-card shadow="hover" class="content-card" @click="openDetail(item)">
          <div class="item-title">{{ item[titleKey] }}</div>
          <div class="item-summary">{{ item.summary }}</div>
          <div class="item-meta">
            <el-tag v-if="item[tagKey]" size="small" type="warning" effect="plain">{{ item[tagKey] }}</el-tag>
            <span class="views">👀 {{ item.viewCount || 0 }}</span>
            <el-tag v-if="item.featured" size="small" type="danger" effect="plain">精选</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="!loading && !list.length" :span="24"><div class="empty-tip">暂无内容，敬请期待 ~</div></el-col>
    </el-row>

    <el-pagination style="justify-content:center" layout="prev, pager, next" :total="total"
                   :page-size="size" v-model:current-page="page" @current-change="load" />

    <el-dialog v-model="dialogVisible" :title="current?.[titleKey]" width="680px" top="6vh">
      <div style="max-height:60vh; overflow-y:auto">
        <div style="margin-bottom:12px">
          <el-tag v-if="current?.[tagKey]" type="warning" effect="plain">{{ current[tagKey] }}</el-tag>
          <span style="color:#909399;font-size:12px;margin-left:10px">阅读 {{ current?.viewCount || 0 }}</span>
        </div>
        <p v-for="(p, i) in paragraphs" :key="i" class="detail-p">{{ p }}</p>
        <div style="margin-top:16px; padding:12px; background:#f0f7ff; border-radius:8px; color:#409eff; font-size:13px">
          💛 如果内容让你有了感触，欢迎去「智能咨询」和倾听伙伴聊聊你的想法。
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import http from '../utils/http'

const props = defineProps({
  api: { type: String, required: true },
  filterParam: { type: String, default: null },
  filters: { type: Array, default: () => [] },
  title: String,
  subtitle: String,
  titleKey: { type: String, default: 'title' },
  tagKey: { type: String, default: 'type' }
})

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 9
const filterValue = ref('')
const dialogVisible = ref(false)
const current = ref(null)

const paragraphs = computed(() => (current.value?.content || '').split('\n').filter(Boolean))

async function load() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size }
    if (props.filterParam && filterValue.value) params[props.filterParam] = filterValue.value
    const data = await http.get(props.api, { params })
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

async function openDetail(item) {
  current.value = await http.get(`${props.api}/${item.id}`)
  dialogVisible.value = true
}

watch(() => props.api, reload)
load()
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.sub { color: #909399; font-size: 13px; margin-left: 12px; }
.content-card { cursor: pointer; height: 172px; }
.item-title { font-weight: 700; font-size: 15px; margin-bottom: 8px; }
.item-summary { color: #606266; font-size: 13px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.item-meta { margin-top: 10px; display: flex; align-items: center; gap: 8px; }
.views { font-size: 12px; color: #909399; }
.detail-p { line-height: 1.9; color: #303133; margin-bottom: 10px; text-indent: 2em; }
</style>
