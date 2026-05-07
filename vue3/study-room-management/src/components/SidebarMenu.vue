<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapsed"
    background-color="transparent"
    text-color="var(--text-secondary)"
    active-text-color="var(--primary-color)"
    class="sidebar-menu"
    router
  >
    <el-menu-item index="/dashboard" class="menu-item">
      <el-icon class="menu-icon"><House /></el-icon>
      <template #title>
        <span class="menu-title">仪表盘</span>
      </template>
    </el-menu-item>

    <el-sub-menu index="room" class="menu-submenu">
      <template #title>
        <el-icon class="menu-icon"><OfficeBuilding /></el-icon>
        <span class="menu-title">自习室管理</span>
      </template>
      <el-menu-item index="/rooms" class="submenu-item">自习室列表</el-menu-item>
      <el-menu-item index="/rooms/add" class="submenu-item">添加自习室</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="seat" class="menu-submenu">
      <template #title>
        <el-icon class="menu-icon"><Chair /></el-icon>
        <span class="menu-title">座位管理</span>
      </template>
      <el-menu-item index="/seats" class="submenu-item">座位列表</el-menu-item>
      <el-menu-item index="/seats/add" class="submenu-item">添加座位</el-menu-item>
    </el-sub-menu>

    <el-sub-menu v-if="!isAdmin" index="reservation" class="menu-submenu">
      <template #title>
        <el-icon class="menu-icon"><Calendar /></el-icon>
        <span class="menu-title">预约管理</span>
      </template>
      <el-menu-item index="/reservations" class="submenu-item">预约列表</el-menu-item>
      <el-menu-item index="/reservations/add" class="submenu-item">新建预约</el-menu-item>
      <el-menu-item index="/reservations/my" class="submenu-item">我的预约</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="user" class="menu-submenu">
      <template #title>
        <el-icon class="menu-icon"><User /></el-icon>
        <span class="menu-title">用户管理</span>
      </template>
      <el-menu-item index="/users" class="submenu-item">用户列表</el-menu-item>
      <el-menu-item index="/users/add" class="submenu-item">添加用户</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="statistics" class="menu-submenu">
      <template #title>
        <el-icon class="menu-icon"><DataAnalysis /></el-icon>
        <span class="menu-title">统计分析</span>
      </template>
      <el-menu-item index="/statistics/overview" class="submenu-item">数据概览</el-menu-item>
      <el-menu-item index="/statistics/usage" class="submenu-item">使用统计</el-menu-item>
      <el-menu-item index="/statistics/revenue" class="submenu-item">收入统计</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/settings" class="menu-item">
      <el-icon class="menu-icon"><Setting /></el-icon>
      <template #title>
        <span class="menu-title">系统设置</span>
      </template>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  House,
  OfficeBuilding,
  Chair,
  Calendar,
  User,
  DataAnalysis,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()

// 判断是否为管理员
const isAdmin = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) return false
  const roleRaw = JSON.parse(userInfo).role
  const userRole = String(roleRaw || '').toUpperCase()
  return ['ADMIN', 'ROLE_ADMIN', '1'].includes(userRole)
})

const isCollapsed = computed(() => {
  // 可以通过props或store传递折叠状态
  return false
})

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
  overflow-y: auto;
  padding: 12px 0;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 280px;
}

/* 菜单项样式 */
.menu-item {
  margin: 4px 12px;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--primary-gradient);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.menu-item:hover {
  background: var(--bg-glass);
  transform: translateX(4px);
}

.menu-item.is-active {
  background: var(--bg-glass);
  color: var(--primary-color);
}

.menu-item.is-active::before {
  opacity: 0.1;
}

.menu-icon {
  font-size: 18px;
  transition: all var(--transition-normal);
}

.menu-item:hover .menu-icon {
  transform: scale(1.1);
  color: var(--primary-color);
}

.menu-title {
  font-weight: 500;
  transition: all var(--transition-normal);
}

.menu-item:hover .menu-title {
  color: var(--primary-color);
}

/* 子菜单样式 */
.menu-submenu {
  margin: 4px 12px;
  border-radius: var(--radius-md);
}

.menu-submenu :deep(.el-sub-menu__title) {
  padding: 12px 16px;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  background: transparent;
}

.menu-submenu :deep(.el-sub-menu__title:hover) {
  background: var(--bg-glass);
  transform: translateX(4px);
}

.menu-submenu :deep(.el-sub-menu__title .el-icon) {
  font-size: 18px;
  transition: all var(--transition-normal);
}

.menu-submenu :deep(.el-sub-menu__title:hover .el-icon) {
  transform: scale(1.1);
  color: var(--primary-color);
}

.submenu-item {
  margin: 2px 12px;
  padding-left: 48px !important;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.submenu-item:hover {
  background: var(--bg-glass);
  transform: translateX(4px);
}

.submenu-item.is-active {
  background: var(--bg-glass);
  color: var(--primary-color);
}

/* 折叠状态样式 */
.el-menu--collapse .menu-item {
  margin: 4px 8px;
  text-align: center;
}

.el-menu--collapse .menu-submenu {
  margin: 4px 8px;
}

/* 自定义滚动条 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
  transition: background var(--transition-fast);
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: var(--primary-color);
}

/* 子菜单展开动画 */
.menu-submenu :deep(.el-menu) {
  background: transparent;
}

.menu-submenu :deep(.el-menu-item) {
  background: transparent;
  color: var(--text-secondary);
}

.menu-submenu :deep(.el-menu-item:hover) {
  background: var(--bg-glass);
  color: var(--primary-color);
}

.menu-submenu :deep(.el-menu-item.is-active) {
  background: var(--bg-glass);
  color: var(--primary-color);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar-menu {
    height: 100vh;
    padding: 8px 0;
  }
  
  .menu-item {
    margin: 2px 8px;
  }
  
  .menu-submenu {
    margin: 2px 8px;
  }
  
  .submenu-item {
    margin: 1px 8px;
    padding-left: 40px !important;
  }
}

/* Element Plus 样式覆盖 */
:deep(.el-menu) {
  background: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  background: transparent;
  border: none;
  color: var(--text-secondary);
}

:deep(.el-menu-item:hover) {
  background: var(--bg-glass);
  color: var(--primary-color);
}

:deep(.el-menu-item.is-active) {
  background: var(--bg-glass);
  color: var(--primary-color);
}

:deep(.el-sub-menu__title) {
  background: transparent;
  border: none;
  color: var(--text-secondary);
}

:deep(.el-sub-menu__title:hover) {
  background: var(--bg-glass);
  color: var(--primary-color);
}

:deep(.el-sub-menu.is-opened .el-sub-menu__title) {
  color: var(--primary-color);
}
</style>
