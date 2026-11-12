import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getRole } from '../utils/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },

  // 学生端
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    children: [
      { path: 'home', name: 'home', component: () => import('../views/student/Home.vue'), meta: { role: 'STUDENT' } },
      { path: 'chat', name: 'chat', component: () => import('../views/student/Chat.vue'), meta: { role: 'STUDENT' } },
      { path: 'stories', name: 'stories', component: () => import('../views/student/Stories.vue'), meta: { role: 'STUDENT' } },
      { path: 'exam', name: 'exam', component: () => import('../views/student/Exam.vue'), meta: { role: 'STUDENT' } },
      { path: 'articles', name: 'articles', component: () => import('../views/student/Articles.vue'), meta: { role: 'STUDENT' } },
      { path: 'surveys', name: 'surveys', component: () => import('../views/student/Surveys.vue'), meta: { role: 'STUDENT' } },
      { path: 'profile', name: 'profile', component: () => import('../views/student/Profile.vue'), meta: { role: 'STUDENT' } }
    ]
  },

  // 咨询师端
  {
    path: '/counselor',
    component: () => import('../layouts/CounselorLayout.vue'),
    children: [
      { path: 'workbench', name: 'workbench', component: () => import('../views/counselor/Workbench.vue'), meta: { role: 'COUNSELOR' } },
      { path: 'sessions', name: 'riskSessions', component: () => import('../views/counselor/RiskSessions.vue'), meta: { role: 'COUNSELOR' } },
      { path: 'cases', name: 'cases', component: () => import('../views/counselor/Cases.vue'), meta: { role: 'COUNSELOR' } }
    ]
  },

  // 管理员端
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      { path: 'dashboard', name: 'adminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { role: 'ADMIN' } },
      { path: 'users', name: 'adminUsers', component: () => import('../views/admin/Users.vue'), meta: { role: 'ADMIN' } },
      { path: 'knowledge', name: 'adminKnowledge', component: () => import('../views/admin/Knowledge.vue'), meta: { role: 'ADMIN' } },
      { path: 'content', name: 'adminContent', component: () => import('../views/admin/ContentManage.vue'), meta: { role: 'ADMIN' } },
      { path: 'qa', name: 'adminQa', component: () => import('../views/admin/QaRecords.vue'), meta: { role: 'ADMIN' } },
      { path: 'analytics', name: 'adminAnalytics', component: () => import('../views/admin/Analytics.vue'), meta: { role: 'ADMIN' } },
      { path: 'configs', name: 'adminConfigs', component: () => import('../views/admin/Configs.vue'), meta: { role: 'ADMIN' } },
      { path: 'monitor', name: 'adminMonitor', component: () => import('../views/admin/Monitor.vue'), meta: { role: 'ADMIN' } }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()
  if (!getToken()) return next('/login')
  const role = getRole()
  if (to.meta.role && to.meta.role !== role) {
    return next(role === 'ADMIN' ? '/admin/dashboard' : role === 'COUNSELOR' ? '/counselor/workbench' : '/student/home')
  }
  next()
})

export default router
