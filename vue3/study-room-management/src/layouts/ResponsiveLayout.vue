<template>
  <el-container class="app-container">
    <!-- 移动端侧边栏 -->
    <el-drawer
      v-model="mobileDrawerVisible"
      direction="ltr"
      :size="280"
      class="mobile-drawer clean-card"
      v-if="isMobile"
    >
      <template #header>
        <div class="drawer-header">
          <h3 class="heading-3">自习室管理系统</h3>
        </div>
      </template>
      <SidebarMenu />
    </el-drawer>

    <!-- 桌面端侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar clean-card" v-if="!isMobile">
      <div class="logo-container">
        <div class="logo-icon">
          <el-icon :size="28"><OfficeBuilding /></el-icon>
        </div>
        <h2 class="heading-3">自习室管理系统</h2>
      </div>
      <SidebarMenu />
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            v-if="isMobile"
            @click="mobileDrawerVisible = true"
            class="menu-button clean-button secondary"
            circle
            icon="Menu"
          />
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }" class="breadcrumb-item">首页</el-breadcrumb-item>
            <el-breadcrumb-item class="breadcrumb-item">{{ currentPage }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="header-actions">
            <el-tooltip content="通知" placement="bottom">
              <el-button class="action-button clean-button secondary" circle>
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="设置" placement="bottom">
              <el-button class="action-button clean-button secondary" circle>
                <el-icon><Setting /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar" class="user-avatar" />
              <span class="username" v-if="!isMobile">{{ userInfo.realName }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="clean-dropdown">
                <el-dropdown-item v-if="!isAdmin" command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import SidebarMenu from '@/components/SidebarMenu.vue'
import {
  OfficeBuilding,
  Menu,
  Bell,
  Setting,
  User,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式状态
const isMobile = ref(window.innerWidth < 768)
const mobileDrawerVisible = ref(false)
const sidebarCollapsed = ref(false)

// 计算属性
const sidebarWidth = computed(() => {
  return sidebarCollapsed.value ? '64px' : '280px'
})

const currentPage = computed(() => {
  return route.meta.title || route.name || '未知页面'
})

const userInfo = computed(() => {
  return userStore.userInfo || {}
})

// 判断是否为管理员
const isAdmin = computed(() => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (!storedUserInfo) return false
  const roleRaw = JSON.parse(storedUserInfo).role
  const userRole = String(roleRaw || '').toUpperCase()
  return ['ADMIN', 'ROLE_ADMIN', '1'].includes(userRole)
})

// 方法
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    mobileDrawerVisible.value = false
  }
}

// 生命周期
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  background: var(--bg-primary);
}

/* 移动端抽屉样式 */
.mobile-drawer {
  z-index: 2000;
}

.drawer-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.drawer-header h3 {
  margin: 0;
  color: var(--text-primary);
}

/* 侧边栏样式 */
.sidebar {
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  transition: width 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.logo-container {
  padding: var(--spacing-xl) var(--spacing-lg);
  text-align: center;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  margin-bottom: var(--spacing-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--primary-color);
  color: white;
}

.logo-container h2 {
  margin: 0;
  color: var(--text-primary);
}

/* 头部样式 */
.header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.menu-button {
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.breadcrumb {
  font-size: var(--font-sm);
}

.breadcrumb-item {
  color: var(--text-secondary);
}

.breadcrumb-item:hover {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.action-button {
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  background: var(--bg-secondary);
}

.action-button:hover {
  border-color: var(--border-hover);
  color: var(--text-primary);
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}

.user-info:hover {
  background: var(--bg-hover);
}

.user-avatar {
  border: 2px solid var(--border-color);
}

.username {
  color: var(--text-primary);
  font-size: var(--font-sm);
  font-weight: 500;
}

.dropdown-icon {
  color: var(--text-secondary);
  transition: transform var(--transition-fast);
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 主内容区域 */
.main-content {
  background: var(--bg-primary);
  padding: 0;
  overflow-y: auto;
}

.content-wrapper {
  padding: var(--spacing-xl);
  min-height: calc(100vh - 60px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 var(--spacing-md);
  }

  .header-left {
    gap: var(--spacing-md);
  }

  .breadcrumb {
    display: none;
  }

  .content-wrapper {
    padding: var(--spacing-lg);
  }

  .header-actions {
    gap: var(--spacing-xs);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 var(--spacing-md);
  }

  .content-wrapper {
    padding: var(--spacing-md);
  }

  .header-right {
    gap: var(--spacing-md);
  }
}

/* 全局样式覆盖 */
:deep(.el-drawer__body) {
  background: var(--bg-secondary);
}

:deep(.el-dropdown-menu) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm) 0;
}

:deep(.el-dropdown-menu__item) {
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-sm);
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--bg-hover);
  color: var(--primary-color);
}

:deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.el-breadcrumb__inner:hover) {
  color: var(--primary-color);
}

:deep(.el-breadcrumb__separator) {
  color: var(--text-hint);
}
</style>
