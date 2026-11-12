<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <div class="side">
      <el-button type="primary" style="width:100%" @click="newSession">＋ 新对话</el-button>
      <div class="session-list">
        <div v-for="s in sessions" :key="s.id" class="session-item" :class="{ active: s.id === sessionId }"
             @click="switchSession(s.id)">
          <div class="session-title">{{ s.title || '未命名对话' }}</div>
          <div class="session-meta">
            <el-tag v-if="s.riskLevel === 'HIGH'" type="danger" size="small">风险</el-tag>
            <el-tag v-else-if="s.riskLevel === 'MEDIUM'" type="warning" size="small">关注</el-tag>
            <el-tag v-if="s.status === 'ESCALATED'" type="info" size="small">已转人工</el-tag>
            <span class="time">{{ fmt(s.updatedAt) }}</span>
          </div>
        </div>
        <div v-if="!sessions.length" class="empty-tip">还没有对话，说出心事开始第一次聊聊吧</div>
      </div>
    </div>

    <!-- 聊天主区 -->
    <div class="main">
      <div ref="bodyRef" class="body">
        <div v-for="m in messages" :key="m.key" class="msg" :class="m.role">
          <div class="avatar">{{ m.role === 'user' ? '🙋' : '🐰' }}</div>
          <div class="bubble-wrap">
            <div class="bubble" :class="{ crisis: m.risk }">{{ m.content }}</div>
            <div v-if="m.role === 'assistant' && m.source" class="source-tag">
              {{ m.source === 'DeepSeek' ? '由 DeepSeek 大模型生成' : m.source === 'CRISIS_PROTOCOL' ? '危机安抚流程' : '心理知识库回答' }}
            </div>
          </div>
        </div>
        <div v-if="sending" class="msg assistant">
          <div class="avatar">🐰</div>
          <div class="bubble typing">正在认真听你说…</div>
        </div>
      </div>

      <el-alert v-if="riskBanner === 'HIGH'" type="error" :closable="false" class="risk-alert" show-icon>
        <template #title>
          我们很担心你的安全。心理老师已收到提醒，会尽快联系你。紧急情况请拨打 12355 / 12356（24 小时）。
          <el-button size="small" type="danger" plain style="margin-left:10px" @click="escalate">联系心理老师</el-button>
        </template>
      </el-alert>
      <el-alert v-else-if="riskBanner === 'MEDIUM'" type="warning" :closable="false" class="risk-alert" show-icon
                title="你的情绪似乎有些波动，记得多照顾自己；需要的话可以点击「转咨询师」和真人老师聊聊。" />

      <div class="suggests" v-if="suggestions.length">
        <el-tag v-for="s in suggestions" :key="s" class="suggest" effect="plain" @click="input = s">{{ s }}</el-tag>
      </div>

      <div class="input-bar">
        <el-input v-model="input" type="textarea" :rows="2" resize="none" maxlength="500" show-word-limit
                  placeholder="在这里说出你的心事，回车发送（Shift+Enter 换行）"
                  @keydown.enter.exact.prevent="send" />
        <div class="input-actions">
          <el-button link type="warning" :disabled="!sessionId || sending" @click="escalate">
            <el-icon><Service /></el-icon>&nbsp;转咨询师
          </el-button>
          <el-button type="primary" :loading="sending" @click="send">发 送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const WELCOME = { role: 'assistant', content: '你好呀，我是心理小屋的倾听伙伴🐰。这里没有评判，只有陪伴。发生了什么，或者最近有什么压在心上？说出来我们一起看看。', key: 'welcome' }

const sessions = ref([])
const messages = ref([{ ...WELCOME }])
const sessionId = ref(null)
const input = ref('')
const sending = ref(false)
const riskBanner = ref('NONE')
const suggestions = ref([])
const bodyRef = ref(null)

function fmt(t) { return t ? String(t).replace('T', ' ').slice(5, 16) : '' }

async function loadSessions() {
  sessions.value = await http.get('/student/chat/sessions')
}

async function newSession() {
  sessionId.value = null
  messages.value = [{ ...WELCOME }]
  riskBanner.value = 'NONE'
  suggestions.value = []
}

async function switchSession(id) {
  sessionId.value = id
  const msgs = await http.get(`/student/chat/sessions/${id}/messages`)
  messages.value = msgs.map(m => ({ role: m.role, content: m.content, source: m.source, risk: m.role === 'user' && m.riskFlag, key: m.id }))
  const s = sessions.value.find(x => x.id === id)
  riskBanner.value = s ? s.riskLevel : 'NONE'
  scrollBottom()
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text, key: Date.now() })
  sending.value = true
  scrollBottom()
  try {
    const reply = await http.post('/student/chat/ask', { sessionId: sessionId.value, content: text })
    sessionId.value = reply.sessionId
    riskBanner.value = reply.riskLevel
    suggestions.value = reply.suggestions || []
    await typewrite(reply)
    loadSessions()
  } finally {
    sending.value = false
  }
}

/** 打字机流式效果 */
function typewrite(reply) {
  const item = { role: 'assistant', content: '', source: reply.source, risk: reply.riskLevel === 'HIGH', key: reply.messageId }
  messages.value.push(item)
  return new Promise(resolve => {
    const full = reply.reply
    let i = 0
    const timer = setInterval(() => {
      const step = Math.max(2, Math.floor(full.length / 60))
      item.content = full.slice(0, i + step)
      i += step
      scrollBottom()
      if (i >= full.length) {
        item.content = full
        clearInterval(timer)
        resolve()
      }
    }, 30)
  })
}

async function escalate() {
  if (!sessionId.value) return ElMessage.info('先和我聊几句，再转给心理老师好吗')
  await http.post(`/student/chat/sessions/${sessionId.value}/escalate`)
  ElMessage.success('已为你预约心理老师，请留意平台与老师的联系')
  loadSessions()
}

function scrollBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

onMounted(loadSessions)
</script>

<style scoped>
.chat-page { display: flex; height: calc(100vh - 76px); background: #fff; }
.side { width: 240px; border-right: 1px solid #ebeef5; padding: 14px; display: flex; flex-direction: column; background: #fafbfd; }
.session-list { margin-top: 12px; overflow-y: auto; flex: 1; }
.session-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; margin-bottom: 6px; }
.session-item:hover { background: #eef2fd; }
.session-item.active { background: #e0eaff; }
.session-title { font-size: 13px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-meta { display: flex; align-items: center; gap: 4px; margin-top: 4px; }
.time { font-size: 11px; color: #a8abb2; margin-left: auto; }
.main { flex: 1; display: flex; flex-direction: column; position: relative; }
.body { flex: 1; overflow-y: auto; padding: 20px 28px; background: linear-gradient(180deg, #f8faff 0%, #fdfcff 100%); }
.msg { display: flex; margin-bottom: 18px; }
.msg.user { flex-direction: row-reverse; }
.avatar { font-size: 26px; margin: 0 10px; }
.bubble-wrap { max-width: 66%; }
.bubble { padding: 12px 16px; border-radius: 14px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; background: #fff; border: 1px solid #ebeef5; box-shadow: 0 1px 3px rgba(0,0,0,.04); }
.msg.user .bubble { background: #4f7cff; color: #fff; border: none; }
.bubble.crisis { background: #fff2f0; border: 1px solid #ffccc7; }
.bubble.typing { color: #909399; }
.source-tag { font-size: 11px; color: #b1b3b8; margin-top: 4px; }
.risk-alert { margin: 0 20px 8px; }
.suggests { padding: 0 24px 8px; display: flex; gap: 8px; flex-wrap: wrap; }
.suggest { cursor: pointer; }
.suggest:hover { color: #4f7cff; border-color: #4f7cff; }
.input-bar { border-top: 1px solid #ebeef5; padding: 12px 20px 14px; }
.input-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
</style>
