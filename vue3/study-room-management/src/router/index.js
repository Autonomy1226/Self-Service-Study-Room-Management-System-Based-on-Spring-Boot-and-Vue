import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true, userPath: ['用户中心', '学习总览', '首页'] }
    },
    {
      path: '/study-rooms',
      name: 'study-rooms',
      component: () => import('../views/StudyRoomsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/seats',
      name: 'seats',
      component: () => import('../views/SeatsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, adminPath: ['管理后台', '资源管理', '座位管理'] }
    },
    {
      path: '/reservations',
      name: 'reservations',
      component: () => import('../views/ReservationsView.vue'),
      meta: { requiresAuth: true, requiresUser: true, userPath: ['用户中心', '学习任务', '预约'] }
    },
    {
      path: '/records',
      name: 'records',
      component: () => import('../views/RecordsView.vue'),
      meta: { requiresAuth: true, requiresUser: true, userPath: ['用户中心', '学习任务', '订单记录'] }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true, requiresUser: true, userPath: ['用户中心', '个人设置', '个人中心'] }
    },
    {
      path: '/announcements',
      name: 'announcements',
      component: () => import('../views/AnnouncementsView.vue'),
      meta: { requiresAuth: true, userPath: ['用户中心', '信息服务', '公告'] }
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/NotificationsView.vue'),
      meta: { requiresAuth: true, userPath: ['用户中心', '信息服务', '通知'] }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, adminPath: ['管理后台', '工作台', '首页'] }
    },
    {
      path: '/tech-dashboard',
      name: 'techDashboard',
      component: () => import('../views/TechDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, adminPath: ['管理后台', '数据中心', '数据仪表盘'] }
    },
    {
      path: '/analytics',
      name: 'analytics',
      component: () => import('../views/AnalyticsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, adminPath: ['管理后台', '数据中心', '数据分析'] }
    },
    {
      path: '/payment',
      name: 'payment',
      component: () => import('../views/PaymentView.vue'),
      meta: { requiresAuth: true, requiresUser: true }
    },
    {
      path: '/third-party-payment',
      name: 'thirdPartyPayment',
      component: () => import('../views/ThirdPartyPaymentView.vue'),
      meta: { requiresAuth: true, requiresUser: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { guest: true }
    }
  ]
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  void from
  const userInfo = localStorage.getItem('userInfo')
  const isAuthenticated = !!userInfo
  const roleRaw = userInfo ? JSON.parse(userInfo).role : null
  const userRole = String(roleRaw || '').toUpperCase()
  const isAdmin = ['ADMIN', 'ROLE_ADMIN', '1'].includes(userRole)

  // 管理员访问根路径时，自动进入管理员工作台
  if (to.path === '/' && isAuthenticated && isAdmin) {
    next('/dashboard')
  }
  // 需要登录的页面
  else if (to.meta.requiresAuth && !isAuthenticated) {
    ElMessage.warning('请先登录')
    next('/login')
  }
  // 需要管理员权限的页面
  else if (to.meta.requiresAdmin && !isAdmin) {
    ElMessage.warning('需要管理员权限')
    next('/')
  }
  // 需要用户权限的页面（管理员不能访问）
  else if (to.meta.requiresUser && isAdmin) {
    ElMessage.warning('该页面仅限普通用户访问')
    next('/dashboard')
  }
  // 游客页面（登录和注册）
  else if (to.meta.guest && isAuthenticated) {
    next(isAdmin ? '/dashboard' : '/')
  }
  else {
    next()
  }
})

export default router