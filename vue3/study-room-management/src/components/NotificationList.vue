<template>
  <div class="notification-list">
    <div v-if="notifications.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Bell /></el-icon>
      <p>暂无通知</p>
    </div>
    
    <div v-else class="notification-items">
      <div 
        v-for="notification in notifications" 
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleClick(notification)"
      >
        <div class="notification-icon" :class="getNotificationType(notification.type)">
          <el-icon v-if="notification.type === 'RESERVATION_REMINDER'"><Clock /></el-icon>
          <el-icon v-else-if="notification.type === 'RESERVATION_EXPIRED'"><Warning /></el-icon>
          <el-icon v-else-if="notification.type === 'RESERVATION_STARTED'"><VideoPlay /></el-icon>
          <el-icon v-else-if="notification.type === 'RESERVATION_CANCELLED'"><Close /></el-icon>
          <el-icon v-else-if="notification.type === 'ANNOUNCEMENT'"><Bell /></el-icon>
          <el-icon v-else><Message /></el-icon>
        </div>
        
        <div class="notification-content">
          <h4 class="notification-title">{{ notification.title }}</h4>
          <p class="notification-desc">{{ notification.content }}</p>
          <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
        </div>
        
        <div class="notification-actions">
          <el-button 
            v-if="!notification.isRead" 
            type="primary" 
            link 
            @click.stop="$emit('mark-read', notification.id)"
          >
            标为已读
          </el-button>
          <el-dropdown @command="handleCommand" trigger="click" @click.stop>
            <el-button link>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="`mark-read-${notification.id}`" v-if="!notification.isRead">
                  标为已读
                </el-dropdown-item>
                <el-dropdown-item :command="`delete-${notification.id}`">
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <div v-if="!notification.isRead" class="unread-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Bell, Clock, Warning, Message, MoreFilled, Close, VideoPlay } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps({
  notifications: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['mark-read', 'delete'])

const getNotificationType = (type) => {
  const types = {
    'RESERVATION_REMINDER': 'reminder',
    'RESERVATION_EXPIRED': 'expired',
    'RESERVATION_STARTED': 'started',
    'RESERVATION_CANCELLED': 'cancelled',
    'ANNOUNCEMENT': 'announcement',
    'SYSTEM': 'system'
  }
  return types[type] || 'default'
}

const formatTime = (time) => {
  const now = dayjs()
  const notificationTime = dayjs(time)
  const diffHours = now.diff(notificationTime, 'hour')
  
  if (diffHours < 1) {
    const diffMinutes = now.diff(notificationTime, 'minute')
    return diffMinutes < 1 ? '刚刚' : `${diffMinutes}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffHours < 48) {
    return '昨天'
  } else {
    return notificationTime.format('MM-DD')
  }
}

const handleClick = (notification) => {
  if (!notification.isRead) {
    emit('mark-read', notification.id)
  }
}

const handleCommand = (command) => {
  const [action, id] = command.split('-')
  if (action === 'mark') {
    emit('mark-read', parseInt(id))
  } else if (action === 'delete') {
    emit('delete', parseInt(id))
  }
}
</script>

<style scoped>
.notification-list {
  min-height: 200px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d1d5db;
}

.notification-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.notification-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.notification-item.unread {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.notification-icon.reminder {
  background: #fef3c7;
  color: #d97706;
}

.notification-icon.expired {
  background: #fee2e2;
  color: #ef4444;
}

.notification-icon.started {
  background: #d1fae5;
  color: #059669;
}

.notification-icon.cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.notification-icon.announcement {
  background: #dbeafe;
  color: #3b82f6;
}

.notification-icon.system {
  background: #f3f4f6;
  color: #6b7280;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.notification-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.unread-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
}
</style>
