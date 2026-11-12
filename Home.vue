<template>
  <div class="page">
    <el-card class="hello-card" shadow="never">
      <div class="hello">你好，{{ nickname }} 👋</div>
      <div class="mood">今天的感觉怎么样？不管开心还是难过，这里都愿意听你说。</div>
      <el-button type="primary" round size="large" @click="$router.push('/student/chat')">
        <el-icon style="margin-right:6px"><ChatDotRound /></el-icon>开始智能咨询
      </el-button>
    </el-card>

    <el-row :gutter="16" style="margin-top:20px">
      <el-col :span="8" v-for="m in modules" :key="m.path">
        <el-card class="module" shadow="hover" @click="$router.push(m.path)">
          <div class="module-icon">{{ m.icon }}</div>
          <div class="module-name">{{ m.name }}</div>
          <div class="module-desc">{{ m.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px" shadow="never">
      <div class="card-title">💡 心理小知识</div>
      <el-carousel height="70px">
        <el-carousel-item v-for="t in tips" :key="t">
          <div class="tip">{{ t }}</div>
        </el-carousel-item>
      </el-carousel>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getUser } from '../../utils/auth'

const nickname = computed(() => getUser().nickname || getUser().username || '同学')

const modules = [
  { path: '/student/stories', name: '榜样故事栏', icon: '🌟', desc: '看看同龄人与名人如何穿越低谷' },
  { path: '/student/exam', name: '高考冲锋专栏', icon: '🎯', desc: '考前心理冲刺与减压实战技巧' },
  { path: '/student/articles', name: '成长驿站科普', icon: '🌱', desc: '读懂情绪，读懂正在长大的自己' },
  { path: '/student/surveys', name: '自我探索', icon: '🧭', desc: '心理测评小问卷，看见自己的状态' },
  { path: '/student/profile', name: '个人中心', icon: '📋', desc: '资料管理与历史咨询记录' },
  { path: '/student/chat', name: '智能咨询', icon: '💬', desc: 'AI 倾听伙伴 24 小时在线，可转人工' }
]

const tips = [
  '适度的紧张能提升表现，别害怕考前心跳加速，那是身体在帮你进入状态。',
  '情绪没有好坏之分，它们只是信使——愤怒在提醒你边界被侵犯，低落在被邀请休息。',
  '睡前十分钟把手机放远一点，深睡眠会替你整理白天学到的知识。',
  '和昨天的自己比，进步哪怕一点点，也值得被自己看见。',
  '求助不是软弱，而是解决问题的勇气。12355 青少年热线随时在线。'
]
</script>

<style scoped>
.hello-card { background: linear-gradient(120deg, #7f9cf5 0%, #a78bfa 60%, #f0abfc 100%); border: none; color: #fff; }
.hello { font-size: 24px; font-weight: 700; }
.mood { margin: 8px 0 18px; opacity: .92; }
.module { text-align: center; cursor: pointer; margin-bottom: 16px; transition: transform .2s; }
.module:hover { transform: translateY(-4px); }
.module-icon { font-size: 34px; }
.module-name { font-weight: 700; margin: 8px 0 4px; }
.module-desc { font-size: 12px; color: #909399; }
.tip { display: flex; align-items: center; height: 100%; padding: 0 16px; color: #606266; }
</style>
