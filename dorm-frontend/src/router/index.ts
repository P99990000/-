import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import MainLayout from '../layouts/MainLayout.vue'
import LoginView from '../views/LoginView.vue'
import StudentScoreView from '../views/StudentScoreView.vue'
import InspectorSubmitView from '../views/InspectorSubmitView.vue'
import InspectorRectificationView from '../views/InspectorRectificationView.vue'
import InspectorRecordView from '../views/InspectorRecordView.vue'
import BigScreenView from '../views/BigScreenView.vue'
import HygieneReportView from '../views/HygieneReportView.vue'
import ChatView from '../views/ChatView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'root',
          redirect: () => {
            const role = localStorage.getItem('user_role')
            if (role === 'student') return '/student/home'
            if (role === 'inspector') return '/inspector/submit'
            if (role === 'admin') return '/admin/dashboard'
            return '/login'
          }
        },
        {
          path: 'student/home',
          name: 'student-home',
          component: StudentScoreView,
          meta: { title: '查分首页', roles: ['student'] }
        },
        {
          path: 'inspector/submit',
          name: 'inspector-submit',
          component: InspectorSubmitView,
          meta: { title: '检查录入', roles: ['inspector', 'admin'] }
        },
        {
          path: 'inspector/records',
          name: 'inspector-records',
          component: InspectorRecordView,
          meta: { title: '检查记录', roles: ['inspector', 'admin'] }
        },
        {
          path: 'inspector/rectification-review',
          name: 'inspector-rectification-review',
          component: InspectorRectificationView,
          meta: { title: '不合格宿舍整改反馈', roles: ['inspector', 'admin'] }
        },
        {
          path: 'admin/dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/AdminDashboardView.vue'),
          meta: { title: '管理员仪表盘', roles: ['admin'] }
        },
        {
          path: 'hygiene-report',
          name: 'hygiene-report',
          component: HygieneReportView,
          meta: { title: '卫生通报', roles: ['student', 'inspector', 'admin'] }
        },
        {
          path: 'chat',
          name: 'chat',
          component: ChatView,
          meta: { title: '消息中心', roles: ['student', 'inspector', 'admin'] }
        },
        // Also support old route /big-screen inside layout if requested, but better standalone.
        // I will map big-screen as a separate root route below, but link to it from menu.
      ]
    },
    {
      path: '/big-screen',
      name: 'big-screen',
      component: BigScreenView,
      meta: { title: '数据大屏', roles: ['admin'] }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const publicPages = ['/login']
  const authRequired = !publicPages.includes(to.path) && !to.meta.public

  if (authRequired && !userStore.isAuthenticated) {
    return next('/login')
  }

  // Role check
  if (to.meta.roles && userStore.role) {
    const roles = to.meta.roles as string[]
    if (!roles.includes(userStore.role)) {
       // Allow BigScreen for Admin only
       // If user is not allowed, redirect to their home
       const role = userStore.role
       if (role === 'student') return next('/student/home')
       if (role === 'inspector') return next('/inspector/submit')
       if (role === 'admin') return next('/admin/dashboard')
       return next('/login')
    }
  }

  next()
})

export default router
