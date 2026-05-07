<template>
  <el-container :class="['app-container', isAdmin ? 'admin-mode' : 'user-mode']">
    <el-header>
      <div class="header-content">
        <div class="logo">自习室管理系统</div>
        <!-- 移动端汉堡菜单按钮 -->
        <el-button 
          class="menu-toggle" 
          @click="toggleMobileMenu"
          v-if="isMobile"
        >
          <el-icon><Menu /></el-icon>
        </el-button>
        <!-- PC端导航菜单 -->
        <el-menu
          v-if="!isMobile"
          mode="horizontal"
          :router="true"
          :default-active="activeMenu"
          class="nav-menu"
        >
          <el-menu-item :index="isAdmin ? '/dashboard' : '/'">
            <el-icon><HomeFilled /></el-icon>
            <span>{{ isAdmin ? '工作台' : '首页' }}</span>
          </el-menu-item>
          <el-menu-item index="/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告</span>
          </el-menu-item>
          <el-menu-item index="/notifications">
            <el-icon><Message /></el-icon>
            <span>通知</span>
          </el-menu-item>
          <el-menu-item index="/study-rooms">
            <el-icon><School /></el-icon>
            <span>自习室</span>
          </el-menu-item>
          <el-menu-item index="/seats" v-if="isAdmin">
            <el-icon><Position /></el-icon>
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="/reservations" v-if="!isAdmin">
            <el-icon><Calendar /></el-icon>
            <span>预约</span>
          </el-menu-item>
          <el-menu-item index="/records" v-if="!isAdmin">
            <el-icon><Document /></el-icon>
            <span>订单记录</span>
          </el-menu-item>
          <el-menu-item index="/payment" v-if="!isAdmin">
            <el-icon><Wallet /></el-icon>
            <span>支付中心</span>
          </el-menu-item>
          <el-menu-item index="/tech-dashboard" v-if="isAdmin">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/analytics" v-if="isAdmin">
            <el-icon><TrendCharts /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
        </el-menu>
        <div class="user-info">
          <div v-if="isAuthenticated" class="role-badge" :class="isAdmin ? 'role-admin' : 'role-user'">
            {{ isAdmin ? '管理员端' : '用户端' }}
          </div>
          <!-- 未登录状态 -->
          <template v-if="!isAuthenticated">
            <el-button type="primary" link @click="handleLogin">登录</el-button>
            <el-button type="primary" link @click="handleRegister">注册</el-button>
          </template>
          <!-- 已登录状态 -->
          <template v-else>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" v-if="!isAdmin">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <div v-if="showAdminWorkbench" class="admin-workbench">
      <div class="admin-workbench-inner">
        <div class="admin-top-row">
          <el-breadcrumb separator=">" class="admin-breadcrumb">
            <el-breadcrumb-item v-for="(item, index) in adminPath" :key="`${item}-${index}`">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="admin-identity">
            <el-icon><Setting /></el-icon>
            <span>管理员工作台</span>
          </div>
        </div>
        <div class="admin-action-bar">
          <div class="admin-filter-group">
            <el-input v-model="adminKeyword" placeholder="筛选关键词" clearable size="small" />
            <el-select v-model="adminStatus" placeholder="状态" clearable size="small" style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="进行中" value="active" />
              <el-option label="已完成" value="done" />
            </el-select>
            <el-button size="small" @click="resetAdminFilters">重置筛选</el-button>
          </div>
          <div class="admin-batch-group">
            <el-button size="small">批量导出</el-button>
            <el-button type="primary" size="small">批量操作</el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showUserTaskBar" class="user-taskbar">
      <div class="user-taskbar-inner">
        <el-breadcrumb separator=">" class="user-breadcrumb">
          <el-breadcrumb-item v-for="(item, index) in userPath" :key="`${item}-${index}`">
            {{ item }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="user-task-actions">
          <el-button text @click="router.push('/reservations')" v-if="!isAdmin">去预约</el-button>
          <el-button text @click="router.push('/records')" v-if="!isAdmin">看记录</el-button>
          <el-button text @click="router.push('/profile')" v-if="!isAdmin">个人设置</el-button>
        </div>
      </div>
    </div>

    <!-- 移动端导航抽屉 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      size="80%"
      :with-header="false"
      class="mobile-menu-drawer"
    >
      <div class="mobile-menu-content">
        <div class="mobile-menu-header">
          <div class="logo">自习室管理系统</div>
          <el-button 
            class="close-menu" 
            @click="mobileMenuVisible = false"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <el-menu
          mode="vertical"
          :router="true"
          :default-active="activeMenu"
          class="mobile-nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item :index="isAdmin ? '/dashboard' : '/'">
            <el-icon><HomeFilled /></el-icon>
            <span>{{ isAdmin ? '工作台' : '首页' }}</span>
          </el-menu-item>
          <el-menu-item index="/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告</span>
          </el-menu-item>
          <el-menu-item index="/notifications">
            <el-icon><Message /></el-icon>
            <span>通知</span>
          </el-menu-item>
          <el-menu-item index="/study-rooms">
            <el-icon><School /></el-icon>
            <span>自习室</span>
          </el-menu-item>
          <el-menu-item index="/seats" v-if="isAdmin">
            <el-icon><Position /></el-icon>
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="/reservations" v-if="!isAdmin">
            <el-icon><Calendar /></el-icon>
            <span>预约</span>
          </el-menu-item>
          <el-menu-item index="/records" v-if="!isAdmin">
            <el-icon><Document /></el-icon>
            <span>订单记录</span>
          </el-menu-item>
          <el-menu-item index="/payment" v-if="!isAdmin">
            <el-icon><Wallet /></el-icon>
            <span>支付中心</span>
          </el-menu-item>
          <el-menu-item index="/dashboard" v-if="isAdmin">
            <el-icon><HomeFilled /></el-icon>
            <span>管理工作台</span>
          </el-menu-item>
          <el-menu-item index="/tech-dashboard" v-if="isAdmin">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/analytics" v-if="isAdmin">
            <el-icon><TrendCharts /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
        </el-menu>
        <div class="mobile-user-info">
          <div v-if="isAuthenticated" class="role-badge" :class="isAdmin ? 'role-admin' : 'role-user'">
            {{ isAdmin ? '管理员端' : '用户端' }}
          </div>
          <template v-if="!isAuthenticated">
            <el-button type="primary" @click="handleLogin">登录</el-button>
            <el-button type="primary" @click="handleRegister">注册</el-button>
          </template>
          <template v-else>
            <div class="mobile-user-profile">
              <el-icon><User /></el-icon>
              <span>{{ username }}</span>
            </div>
            <el-button type="primary" @click="handleCommand('profile')" v-if="!isAdmin">
              个人中心
            </el-button>
            <el-button type="danger" @click="handleCommand('logout')">
              退出登录
            </el-button>
          </template>
        </div>
      </div>
    </el-drawer>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowDown, 
  User, 
  SwitchButton, 
  Menu, 
  Close,
  HomeFilled,
  School,
  Position,
  Calendar,
  Document,
  Bell,
  Message,
  Wallet,
  DataAnalysis,
  TrendCharts,
  Setting
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 移动端菜单控制
const isMobile = ref(false)
const mobileMenuVisible = ref(false)

// 检查是否为移动设备
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 切换移动端菜单
const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

// 处理菜单选择
const handleMenuSelect = () => {
  mobileMenuVisible.value = false
}

// 从localStorage获取用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
const isAuthenticated = computed(() => !!userInfo.value)
const username = computed(() => userInfo.value?.username || '未登录')
const activeMenu = computed(() => route.path)

const normalizeRole = (role) => String(role || '').toUpperCase()

const isAdmin = computed(() => {
  const roleFromState = normalizeRole(userInfo.value?.role)
  const roleFromStorage = normalizeRole(JSON.parse(localStorage.getItem('userInfo') || 'null')?.role)
  const currentRole = roleFromState || roleFromStorage
  const isAdminUser = ['ADMIN', 'ROLE_ADMIN', '1'].includes(currentRole)
  console.log('App.vue - 当前角色:', currentRole, '是否管理员:', isAdminUser)
  return isAdminUser
})

const showAdminWorkbench = computed(() => false)
const showUserTaskBar = computed(() => isAuthenticated.value && !isAdmin.value && route.meta?.requiresAuth)
const adminPath = computed(() => route.meta?.adminPath || ['管理后台', '数据中心', '数据仪表盘'])
const userPath = computed(() => route.meta?.userPath || ['用户中心', '当前页面'])
const adminKeyword = ref('')
const adminStatus = ref('')

const resetAdminFilters = () => {
  adminKeyword.value = ''
  adminStatus.value = ''
}

// 监听登录状态变化
const handleLoginStateChange = (event) => {
  console.log('App.vue - 登录状态变化事件:', event.detail)
  if (event.detail.userInfo) {
    userInfo.value = event.detail.userInfo
  } else {
    const newUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
    userInfo.value = newUserInfo
  }
  console.log('App.vue - 更新后的用户信息:', userInfo.value)
}

onMounted(() => {
  window.addEventListener('login-state-change', handleLoginStateChange)
  // 初始化时检查用户信息
  const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  console.log('App.vue - 初始化用户信息:', currentUserInfo)
  userInfo.value = currentUserInfo
  // 强制更新 isAdmin 计算属性
  nextTick(() => {
    console.log('App.vue - 初始化后的管理员状态:', isAdmin.value)
  })
})

onUnmounted(() => {
  window.removeEventListener('login-state-change', handleLoginStateChange)
})

// 处理登录按钮点击
const handleLogin = () => {
  router.push('/login')
}

// 处理注册按钮点击
const handleRegister = () => {
  router.push('/register')
}

// 处理用户菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      // 清除所有本地存储的用户信息
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      // 更新用户信息状态
      userInfo.value = null
      // 触发登录状态变化事件
      window.dispatchEvent(new CustomEvent('login-state-change', {
        detail: { userInfo: null }
      }))
      ElMessage.success('退出成功')
      // 强制刷新页面
      window.location.href = '/login'
      break
  }
}
</script>

<style>
:root {
  --primary-color: #5e72e4;
  --primary-dark: #4c63d2;
  --primary-light: #7c8ff0;
  --secondary-color: #6c757d;
  --success-color: #2dce89;
  --info-color: #11cdef;
  --warning-color: #fb6340;
  --danger-color: #f5365c;
  --text-primary: #32325d;
  --text-secondary: #8898aa;
  --text-muted: #adb5bd;
  --bg-color: #ffffff;
  --bg-white: #ffffff;
  --bg-gradient: linear-gradient(87deg, #5e72e4 0, #825ee4 100%);
  --card-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  --nav-shadow: 0 1px 6px rgba(15, 23, 42, 0.08);
  --hover-shadow: 0 2px 8px rgba(15, 23, 42, 0.12);
  --transition-base: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-fast: all 0.15s ease;
  
  /* 添加字体大小变量 */
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;
  
  /* 添加间距变量 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-2xl: 48px;
  
  /* 导航栏特定变量 */
  --nav-height: 70px;
  --nav-padding: 20px;
  --logo-size: 28px;
  --menu-item-height: 60px;
}

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #ffffff;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  line-height: 1.5;
}

.app-container {
  min-height: 100vh;
}

.el-header {
  background: var(--bg-white);
  box-shadow: var(--nav-shadow);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}


.header-content {
  max-width: 1600px;
  margin: 0 auto;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--nav-padding);
  gap: 20px;
}

.logo {
  font-size: var(--logo-size);
  font-weight: 700;
  color: var(--primary-color);
  margin-right: 60px;
  letter-spacing: -0.5px;
  position: relative;
  transition: var(--transition-base);
  white-space: nowrap;
  flex-shrink: 0;
}

.logo::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 3px;
  background: var(--bg-gradient);
  border-radius: 2px;
  transition: var(--transition-base);
}

.logo:hover {
  background: var(--bg-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo:hover::after {
  width: 100%;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}


.nav-menu :deep(.el-menu-item) {
  font-weight: 500;
  color: var(--text-secondary);
  border-radius: 12px;
  margin: 0 8px;
  height: var(--menu-item-height);
  line-height: var(--menu-item-height);
  padding: 0 24px;
  transition: var(--transition-base);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.nav-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  transition: var(--transition-base);
}

.nav-menu :deep(.el-menu-item span) {
  transition: var(--transition-base);
}

.nav-menu :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--bg-gradient);
  opacity: 0;
  transition: var(--transition-base);
  border-radius: 12px;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: var(--primary-color) !important;
  background: rgba(94, 114, 228, 0.08);
  transform: translateY(-1px);
}

.nav-menu :deep(.el-menu-item:hover::before) {
  opacity: 0.1;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: var(--primary-color) !important;
  font-weight: 600;
  background: rgba(94, 114, 228, 0.12);
  box-shadow: 0 4px 12px rgba(94, 114, 228, 0.15);
}

.nav-menu :deep(.el-menu-item.is-active::before) {
  opacity: 0.15;
}

.user-info {
  margin-left: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.role-badge {
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
}

.role-user {
  color: #1d4ed8;
  background: #dbeafe;
  border: 1px solid #bfdbfe;
}

.role-admin {
  color: #1f2937;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--text-primary);
  font-weight: 500;
  padding: 10px 16px;
  border-radius: 12px;
  transition: var(--transition-base);
  background: rgba(94, 114, 228, 0.05);
  border: 1px solid rgba(94, 114, 228, 0.1);
}

.user-dropdown:hover {
  color: var(--primary-color);
  background: rgba(94, 114, 228, 0.1);
  border-color: rgba(94, 114, 228, 0.2);
  transform: translateY(-1px);
  box-shadow: var(--hover-shadow);
}

.user-dropdown .el-icon {
  margin-left: 8px;
  transition: var(--transition-base);
  font-size: 16px;
}

.user-dropdown:hover .el-icon {
  transform: rotate(180deg);
}

.el-main {
  padding: 24px;
  max-width: 1560px;
  margin: 0 auto;
  background: #ffffff;
}

.admin-workbench {
  position: sticky;
  top: var(--nav-height);
  z-index: 900;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.admin-workbench-inner {
  max-width: 1560px;
  margin: 0 auto;
  padding: 10px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.admin-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-breadcrumb {
  flex: 1;
}

.admin-identity {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4b5563;
  font-weight: 600;
  white-space: nowrap;
}

.admin-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.admin-filter-group,
.admin-batch-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-filter-group {
  flex-wrap: wrap;
}

.user-taskbar {
  border-bottom: 1px solid #eef2f7;
  background: #ffffff;
}

.user-taskbar-inner {
  max-width: 1560px;
  margin: 0 auto;
  padding: 8px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.user-breadcrumb {
  flex: 1;
}

.user-task-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 移动端导航样式 */
.menu-toggle {
  display: none;
  padding: 12px;
  font-size: 20px;
  border: none;
  background: transparent;
  color: var(--primary-color);
  border-radius: 12px;
  transition: var(--transition-base);
  cursor: pointer;
}

.menu-toggle:hover {
  background: rgba(94, 114, 228, 0.1);
  transform: scale(1.05);
}

.menu-toggle:active {
  transform: scale(0.95);
}

.mobile-menu-drawer {
  --el-drawer-padding-primary: 0;
}

.mobile-menu-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.mobile-menu-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-white);
}

.mobile-menu-header {
  padding: var(--spacing-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: var(--bg-gradient);
  color: white;
}


.mobile-menu-header .logo {
  color: white;
  margin-right: 0;
}

.close-menu {
  padding: 12px;
  font-size: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 12px;
  transition: var(--transition-base);
  cursor: pointer;
}

.close-menu:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.close-menu:active {
  transform: scale(0.95);
}

.mobile-nav-menu {
  flex: 1;
  border-right: none;
  padding: var(--spacing-md) 0;
}

.mobile-nav-menu :deep(.el-menu-item) {
  height: 56px;
  line-height: 56px;
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 4px var(--spacing-md);
  border-radius: 12px;
  transition: var(--transition-base);
  color: var(--text-secondary);
  font-weight: 500;
}

.mobile-nav-menu :deep(.el-menu-item:hover) {
  background: rgba(94, 114, 228, 0.08);
  color: var(--primary-color);
  transform: translateX(4px);
}

.mobile-nav-menu :deep(.el-menu-item.is-active) {
  background: rgba(94, 114, 228, 0.12);
  color: var(--primary-color);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(94, 114, 228, 0.15);
}


.mobile-nav-menu :deep(.el-menu-item .el-icon) {
  margin-right: 0;
  font-size: 20px;
  color: var(--primary-color);
}

.mobile-user-info {
  padding: var(--spacing-lg);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.mobile-user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-white);
  border-radius: 12px;
  margin-bottom: var(--spacing-sm);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.mobile-user-profile .el-icon {
  font-size: 20px;
  color: var(--primary-color);
}

.mobile-user-info .el-button {
  border-radius: 12px;
  font-weight: 500;
  height: 44px;
  transition: var(--transition-base);
}

.mobile-user-info .el-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--hover-shadow);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .menu-toggle {
    display: block;
  }

  .nav-menu {
    display: none;
  }

  .header-content {
    padding: 0 var(--spacing-md);
    gap: 10px;
  }

  .logo {
    font-size: 18px;
    margin-right: 0;
    flex-shrink: 1;
  }

  .user-info {
    display: none;
  }

  .mobile-user-info .role-badge {
    align-self: flex-start;
  }

  .admin-workbench-inner,
  .user-taskbar-inner {
    padding: 10px var(--spacing-md);
  }

  .admin-top-row,
  .admin-action-bar,
  .user-taskbar-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .el-main {
    padding: var(--spacing-md);
    max-width: 100%;
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .header-content {
    padding: 0 var(--spacing-lg);
    gap: 15px;
  }

  .el-main {
    padding: var(--spacing-lg);
  }

  .nav-menu :deep(.el-menu-item) {
    margin: 0 4px;
    padding: 0 16px;
  }

  .logo {
    font-size: 22px;
    margin-right: 30px;
  }

  .user-info {
    margin-left: 10px;
  }
}

/* 大屏幕适配 */
@media screen and (min-width: 1400px) {
  .header-content {
    padding: 0 var(--spacing-2xl);
  }

  .el-main {
    padding: var(--spacing-2xl);
  }

  .nav-menu :deep(.el-menu-item) {
    margin: 0 10px;
    padding: 0 28px;
  }

  .logo {
    font-size: 30px;
    margin-right: 70px;
  }
}

/* 超大屏幕适配 */
@media screen and (min-width: 1600px) {
  .header-content {
    max-width: 1800px;
  }

  .el-main,
  .admin-workbench-inner,
  .user-taskbar-inner {
    max-width: 1680px;
  }

  .nav-menu :deep(.el-menu-item) {
    margin: 0 12px;
    padding: 0 32px;
  }
}

/* 全局卡片样式优化 */
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: var(--card-shadow);
  transition: var(--transition-base);
  margin-bottom: var(--spacing-lg);
  background: var(--bg-white);
  overflow: hidden;
}

.admin-mode .el-card {
  border-left: 3px solid #d1d5db;
}

.el-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 3rem 0 rgba(136, 152, 170, 0.2);
}

.el-card__header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
}

/* 全局按钮样式优化 */
.el-button {
  font-weight: 500;
  border-radius: 12px;
  transition: var(--transition-base);
  height: auto;
  line-height: 1.4;
  border: 1px solid transparent;
}

.el-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--hover-shadow);
}

.el-button:active {
  transform: translateY(0);
}

.el-button--primary {
  background: var(--bg-gradient);
  border-color: var(--primary-color);
}

.el-button--primary:hover {
  background: var(--primary-dark);
  border-color: var(--primary-dark);
}


/* 全局表单样式优化 */
.el-form-item {
  margin-bottom: var(--spacing-lg);
}

.el-form-item__label {
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: var(--spacing-sm);
}

.el-input__wrapper {
  border-radius: 12px;
  transition: var(--transition-base);
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.06) inset;
}

.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px var(--primary-color) inset;
}

/* 全局表格样式优化 */
.el-table {
  border-radius: 16px;
  overflow: hidden;
  font-size: var(--font-size-sm);
  box-shadow: var(--card-shadow);
}

.el-table th {
  background-color: var(--bg-color) !important;
  font-weight: 600;
  padding: var(--spacing-md);
  color: var(--text-primary);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.el-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.el-table tr:hover > td {
  background-color: rgba(94, 114, 228, 0.02);
}

/* 全局标签样式优化 */
.el-tag {
  border-radius: 8px;
  font-weight: 500;
  line-height: 1.4;
  border: none;
  padding: 6px 12px;
}

/* 全局对话框样式优化 */
.el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--nav-shadow);
}

.el-dialog__header {
  padding: var(--spacing-lg);
  margin: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: var(--bg-color);
}

.el-dialog__body {
  padding: var(--spacing-lg);
}

.el-dialog__footer {
  padding: var(--spacing-lg);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

/* 全局分页样式优化 */
.el-pagination {
  margin-top: var(--spacing-lg);
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

.el-pagination .btn-prev,
.el-pagination .btn-next {
  border-radius: 8px;
}

.el-pagination .el-pager li {
  border-radius: 8px;
  margin: 0 2px;
}

.el-pagination .el-pager li.is-active {
  background: var(--bg-gradient);
  color: white;
}

/* 全局加载动画优化 */
.el-loading-mask {
  backdrop-filter: blur(4px);
  background-color: rgba(255, 255, 255, 0.8);
}

.el-loading-spinner {
  margin-top: -50px;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--text-muted);
  border-radius: 4px;
  transition: var(--transition-base);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}
</style>