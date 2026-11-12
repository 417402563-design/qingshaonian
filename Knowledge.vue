<template>
  <div>
    <el-tabs v-model="tab">
      <!-- 常见问题分类 -->
      <el-tab-pane label="问题分类体系" name="category">
        <div class="toolbar">
          <span class="tip">分类与关键词是智能咨询「知识库检索」的依据，请结合学生真实提问持续运营。</span>
          <el-button type="primary" @click="openCat()">＋ 新增分类</el-button>
        </div>
        <el-card shadow="never" v-loading="catLoading">
          <el-table :data="categories" stripe>
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column prop="name" label="分类名称" width="140" />
            <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
            <el-table-column prop="keywords" label="命中关键词" min-width="260" show-overflow-tooltip />
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="openCat(row)">编辑</el-button>
                <el-button link type="danger" @click="delCat(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 知识库条目 -->
      <el-tab-pane label="知识库条目" name="item">
        <div class="toolbar">
          <el-select v-model="itemFilter" clearable placeholder="按分类筛选" style="width:180px" @change="loadItems">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
          <el-button type="primary" @click="openItem()">＋ 新增条目</el-button>
        </div>
        <el-card shadow="never" v-loading="itemLoading">
          <el-table :data="items" stripe>
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="categoryCode" label="分类" width="120" />
            <el-table-column prop="tags" label="标签" min-width="180" show-overflow-tooltip />
            <el-table-column prop="viewCount" label="浏览" width="80" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
                  {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="openItem(row)">编辑</el-button>
                <el-button link type="danger" @click="delItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:12px; justify-content:flex-end" layout="prev, pager, next"
                         :total="itemTotal" :page-size="10" v-model:current-page="itemPage" @current-change="loadItems" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 分类编辑 -->
    <el-dialog v-model="catVisible" :title="catForm.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="catForm" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="catForm.name" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="catForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="catForm.keywords" placeholder="逗号分隔，如：考试,紧张,失眠" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>

    <!-- 条目编辑 -->
    <el-dialog v-model="itemVisible" :title="itemForm.id ? '编辑条目' : '新增条目'" width="600px">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="标题" required><el-input v-model="itemForm.title" /></el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="itemForm.categoryCode" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文" required><el-input v-model="itemForm.content" type="textarea" :rows="6"
          placeholder="作为大模型回答的知识上下文，建议：共情开场 + 3 条以内可操作方法" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="itemForm.tags" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="itemForm.status">
            <el-radio value="PUBLISHED">发布</el-radio><el-radio value="DRAFT">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const tab = ref('category')
const categories = ref([])
const items = ref([])
const catLoading = ref(true)
const itemLoading = ref(false)
const catVisible = ref(false)
const itemVisible = ref(false)
const itemFilter = ref('')
const itemPage = ref(1)
const itemTotal = ref(0)

const catForm = reactive({ id: null, name: '', description: '', keywords: '', sortOrder: 0 })
const itemForm = reactive({ id: null, title: '', content: '', tags: '', categoryCode: '', status: 'PUBLISHED' })

async function loadCats() {
  catLoading.value = true
  try {
    categories.value = await http.get('/admin/kb/categories')
  } finally {
    catLoading.value = false
  }
}

async function loadItems() {
  itemLoading.value = true
  try {
    const data = await http.get('/content/kb/items', { params: { page: itemPage.value - 1, size: 10, categoryCode: itemFilter.value || undefined } })
    items.value = data.content
    itemTotal.value = data.totalElements
  } finally {
    itemLoading.value = false
  }
}

function openCat(row) {
  Object.assign(catForm, row ? { ...row } : { id: null, name: '', description: '', keywords: '', sortOrder: categories.value.length + 1 })
  catVisible.value = true
}

async function saveCat() {
  if (!catForm.name) return ElMessage.warning('分类名称必填')
  await http.post('/admin/kb/categories', { ...catForm })
  ElMessage.success('已保存')
  catVisible.value = false
  loadCats()
}

async function delCat(row) {
  await ElMessageBox.confirm(`删除分类「${row.name}」可能影响智能咨询的检索命中，确认？`, '警告', { type: 'warning' })
  await http.delete(`/admin/kb/categories/${row.id}`)
  loadCats()
}

function openItem(row) {
  Object.assign(itemForm, row ? { ...row } : { id: null, title: '', content: '', tags: '', categoryCode: '', status: 'PUBLISHED' })
  itemVisible.value = true
}

async function saveItem() {
  if (!itemForm.title || !itemForm.content) return ElMessage.warning('标题与正文必填')
  await http.post('/admin/kb/items', { ...itemForm })
  ElMessage.success('已保存，将作为大模型回答上下文')
  itemVisible.value = false
  loadItems()
}

async function delItem(row) {
  await ElMessageBox.confirm(`确认删除条目「${row.title}」？`, '提示', { type: 'warning' })
  await http.delete(`/admin/kb/items/${row.id}`)
  loadItems()
}

onMounted(() => { loadCats(); loadItems() })
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; }
</style>
