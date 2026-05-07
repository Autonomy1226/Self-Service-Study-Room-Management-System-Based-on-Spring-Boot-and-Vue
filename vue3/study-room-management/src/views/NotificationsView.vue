<template>
  <div class="notifications-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Bell /></el-icon>
        通知中心
      </h2>
      <p class="page-description">查看您的系统通知和消息</p>
    </div>

    <div class="content-card">
      <div class="card-header">
        <h3>通知列表</h3>
        <div class="header-actions">
          <el-button @click="markAllAsRead" :disabled="!hasUnread">
            全部标为已读
          </el-button>
          <el-button @click="clearRead" :disabled="readCount === 0">
            清空已读
          </el-button>
        </div>
      </div>

      <div class="notification-stats">
        <div class="stat-item">
          <span class="stat-value unread">{{ unreadCount }}</span>
          <span class="stat-label">未读</span>
        </div>
        <div class="stat-item">
          <span class="stat-value total">{{ totalCount }}</span>
          <span class="stat-label">总计</span>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="notification-tabs">
        <el-tab-pane label="全部" name="all">
          <NotificationList 
            :notifications="filteredNotifications" 
            @mark-read="handleMarkRead"
            @delete="handleDelete"
          />
        </el-tab-pane>
        <el-tab-pane label="未读" name="unread">
          <NotificationList 
            :notifications="unreadNotifications" 
            @mark-read="handleMarkRead"
            @delete="handleDelete"
          />
        </el-tab-pane>
        <el-tab-pane label="已读" name="read">
          <NotificationList 
            :notifications="readNotifications" 
            @mark-read="handleMarkRead"
            @delete="handleDelete"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { notificationApi } from '../api'
import NotificationList from '../components/NotificationList.vue'

const notifications = ref([])
const activeTab = ref('all')
const loading = ref(false)

const unreadNotifications = computed(() => 
  notifications.value.filter(n => !n.isRead)
)

const readNotifications = computed(() => 
  notifications.value.filter(n => n.isRead)
)

const filteredNotifications = computed(() => {
  switch (activeTab.value) {
    case 'unread':
      return unreadNotifications.value
    case 'read':
      return readNotifications.value
    default:
      return notifications.value
  }
})

const unreadCount = computed(() => unreadNotifications.value.length)
const totalCount = computed(() => notifications.value.length)
const readCount = computed(() => readNotifications.value.length)
const hasUnread = computed(() => unreadCount.value > 0)

const fetchNotifications = async () => {
  loading.value = true
  try {
    const data = await notificationApi.getUserNotifications(getCurrentUserId())
    notifications.value = data.sort((a, b) => 
      new Date(b.createTime) - new Date(a.createTime)
    )
  } catch (error) {
    console.error('获取通知失败:', error)
    ElMessage.error('获取通知失败')
  } finally {
    loading.value = false
  }
}

const getCurrentUserId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.id
}

const handleMarkRead = async (notificationId) => {
  try {
    await notificationApi.markAsRead(notificationId)
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = true
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (notificationId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      type: 'warning'
    })
    await notificationApi.deleteNotification(notificationId)
    notifications.value = notifications.value.filter(n => n.id !== notificationId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const markAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead(getCurrentUserId())
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('全部已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const clearRead = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有已读通知吗？', '提示', {
      type: 'warning'
    })
    const readIds = readNotifications.value.map(n => n.id)
    for (const id of readIds) {
      await notificationApi.deleteNotification(id)
    }
    notifications.value = unreadNotifications.value
    ElMessage.success('已清空已读通知')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notifications-page {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title .el-icon {
  font-size: 32px;
  color: var(--primary-color);
}

.page-description {
  color: var(--text-secondary);
  font-size: 14px;
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.notification-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-value.unread {
  color: #ef4444;
}

.stat-value.total {
  color: #3b82f6;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

.notification-tabs {
  margin-top: 20px;
}
</style>
