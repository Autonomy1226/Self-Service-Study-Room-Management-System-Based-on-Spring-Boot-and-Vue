<template>
  <div class="home-page">
    <!-- 顶部欢迎区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="welcome-section">
          <div class="welcome-badge">
            <el-icon><Star /></el-icon>
            <span>欢迎回来</span>
          </div>
          <h1 class="hero-title">开始高效学习之旅</h1>
          <p class="hero-subtitle">预约专属座位，享受安静舒适的学习环境</p>
          
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-icon blue">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.todayReservations }}</span>
                <span class="stat-label">今日预约</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon green">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.inUseSeats }}</span>
                <span class="stat-label">使用中</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon orange">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.availableSeats }}</span>
                <span class="stat-label">可用座位</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="quick-actions" v-if="!isAdmin">
          <button class="action-btn primary" @click="$router.push('/reservations')">
            <el-icon><Plus /></el-icon>
            <span>立即预约</span>
          </button>
          <button class="action-btn secondary" @click="$router.push('/records')">
            <el-icon><Document /></el-icon>
            <span>查看记录</span>
          </button>
        </div>
      </div>
    </section>

    <!-- 公告区域 -->
    <section class="announcement-section" v-if="announcements.length > 0">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Bell /></el-icon>
          最新公告
        </h2>
        <button class="refresh-btn" @click="$router.push('/announcements')">
          <el-icon><ArrowRight /></el-icon>
          查看全部
        </button>
      </div>
      <div class="announcement-list">
        <div 
          v-for="item in announcements.slice(0, 3)" 
          :key="item.id"
          class="announcement-item"
          @click="showAnnouncementDetail(item)"
        >
          <div class="announcement-icon" :class="item.type?.toLowerCase()">
            <el-icon v-if="item.type === 'URGENT'"><Warning /></el-icon>
            <el-icon v-else-if="item.type === 'MAINTENANCE'"><Tools /></el-icon>
            <el-icon v-else-if="item.type === 'RULE'"><Document /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </div>
          <div class="announcement-content">
            <h4 class="announcement-title">{{ item.title }}</h4>
            <p class="announcement-desc">{{ item.content?.substring(0, 60) }}...</p>
            <span class="announcement-time">{{ formatTime(item.createTime) }}</span>
          </div>
          <el-icon class="announcement-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 数据概览区域 -->
    <section class="metrics-section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><DataLine /></el-icon>
          数据概览
        </h2>
        <button class="refresh-btn" @click="fetchStats">
          <el-icon><Refresh /></el-icon>
          刷新
        </button>
      </div>
      
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-icon blue">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <span class="metric-trend up">+12%</span>
          </div>
          <div class="metric-body">
            <span class="metric-value">{{ stats.openRooms }}</span>
            <span class="metric-label">开放自习室</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-icon green">
              <el-icon><UserFilled /></el-icon>
            </div>
            <span class="metric-trend up">+8%</span>
          </div>
          <div class="metric-body">
            <span class="metric-value">{{ stats.availableSeats }}</span>
            <span class="metric-label">可用座位</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-icon orange">
              <el-icon><Clock /></el-icon>
            </div>
            <span class="metric-trend down">-2%</span>
          </div>
          <div class="metric-body">
            <span class="metric-value">{{ stats.inUseSeats }}</span>
            <span class="metric-label">使用中座位</span>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-header">
            <div class="metric-icon purple">
              <el-icon><Calendar /></el-icon>
            </div>
            <span class="metric-trend up">+15%</span>
          </div>
          <div class="metric-body">
            <span class="metric-value">{{ stats.todayReservations }}</span>
            <span class="metric-label">今日预约</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷功能区域 -->
    <section class="features-section">
      <h2 class="section-title">快捷功能</h2>
      <div class="features-grid">
        <div class="feature-card" v-if="!isAdmin" @click="$router.push('/reservations')">
          <div class="feature-icon blue">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="feature-content">
            <h4>立即预约</h4>
            <p>快速预约心仪座位</p>
          </div>
          <el-icon class="feature-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="feature-card" v-if="!isAdmin" @click="$router.push('/records')">
          <div class="feature-icon green">
            <el-icon><Document /></el-icon>
          </div>
          <div class="feature-content">
            <h4>查看记录</h4>
            <p>管理预约历史</p>
          </div>
          <el-icon class="feature-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="feature-card" @click="$router.push('/study-rooms')">
          <div class="feature-icon orange">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="feature-content">
            <h4>自习室管理</h4>
            <p>查看所有自习室</p>
          </div>
          <el-icon class="feature-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="feature-card" @click="$router.push('/profile')">
          <div class="feature-icon purple">
            <el-icon><User /></el-icon>
          </div>
          <div class="feature-content">
            <h4>个人中心</h4>
            <p>管理个人信息</p>
          </div>
          <el-icon class="feature-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { studyRoomApi, seatApi, reservationApi, announcementApi } from '../api'
import dayjs from 'dayjs'
import {
  Star,
  Calendar,
  Clock,
  OfficeBuilding,
  Plus,
  Document,
  DataLine,
  Refresh,
  UserFilled,
  User,
  ArrowRight,
  Bell,
  Warning,
  Tools
} from '@element-plus/icons-vue'

// 判断是否为管理员
const isAdmin = computed(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) return false
  const roleRaw = JSON.parse(userInfo).role
  const userRole = String(roleRaw || '').toUpperCase()
  return ['ADMIN', 'ROLE_ADMIN', '1'].includes(userRole)
})

const stats = ref({
  openRooms: 0,
  availableSeats: 0,
  todayReservations: 0,
  inUseSeats: 0
})

const announcements = ref([])

const fetchAnnouncements = async () => {
  try {
    const data = await announcementApi.getActiveAnnouncements()
    announcements.value = data.slice(0, 3)
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

const showAnnouncementDetail = (item) => {
  ElMessage.info(`${item.title}: ${item.content}`)
}

const formatTime = (time) => {
  return time ? dayjs(time).format('MM-DD HH:mm') : ''
}

const fetchStats = async () => {
  try {
    const rooms = await studyRoomApi.getAllRooms()
    stats.value.openRooms = rooms.filter(room => room.status === 'OPEN').length

    const allSeats = []
    for (const room of rooms) {
      const seats = await seatApi.getSeatsByRoom(room.id)
      allSeats.push(...seats)
    }
    stats.value.availableSeats = allSeats.filter(seat => seat.status === 'AVAILABLE').length

    const today = dayjs().format('YYYY-MM-DD')
    const startTime = `${today}T00:00:00`
    const endTime = `${today}T23:59:59`

    const todayReservations = await reservationApi.getReservationsByTimeRange(startTime, endTime)
    stats.value.todayReservations = todayReservations.length

    stats.value.inUseSeats = todayReservations.filter(reservation => 
      reservation.status === 'IN_USE'
    ).length

  } catch (error) {
    console.error('获取统计数据失败:', error)
    stats.value = {
      openRooms: 4,
      availableSeats: 45,
      todayReservations: 89,
      inUseSeats: 23
    }
  }
}

onMounted(() => {
  fetchStats()
  fetchAnnouncements()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #ffffff;
  padding: 40px;
}

/* 顶部欢迎区域 */
.hero-section {
  margin-bottom: 48px;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 60px;
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  flex: 1;
}

.welcome-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #eff6ff;
  color: #3b82f6;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 18px;
  color: #6b7280;
  margin-bottom: 32px;
}

.hero-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-icon.green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 280px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 32px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  height: 56px;
}

.action-btn.primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.3);
}

.action-btn.secondary {
  background: white;
  color: #374151;
  border: 2px solid #e5e7eb;
}

.action-btn.secondary:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

/* 数据概览区域 */
.metrics-section {
  max-width: 1400px;
  margin: 0 auto 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.metric-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.metric-icon.blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.metric-icon.green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.metric-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.metric-icon.purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.metric-trend {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}

.metric-trend.up {
  color: #059669;
  background: #d1fae5;
}

.metric-trend.down {
  color: #dc2626;
  background: #fee2e2;
}

.metric-body {
  display: flex;
  flex-direction: column;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 15px;
  color: #9ca3af;
}

/* 快捷功能区域 */
.features-section {
  max-width: 1400px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.feature-icon.blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.feature-icon.green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.feature-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.feature-icon.purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.feature-content {
  flex: 1;
}

.feature-content h4 {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.feature-content p {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.feature-arrow {
  color: #d1d5db;
  font-size: 18px;
  transition: all 0.2s ease;
}

.feature-card:hover .feature-arrow {
  color: #3b82f6;
  transform: translateX(4px);
}

/* 公告区域 */
.announcement-section {
  max-width: 1400px;
  margin: 0 auto 48px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}

.announcement-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.announcement-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.announcement-icon.general {
  background: #eff6ff;
  color: #3b82f6;
}

.announcement-icon.rule {
  background: #fef3c7;
  color: #d97706;
}

.announcement-icon.maintenance {
  background: #fee2e2;
  color: #ef4444;
}

.announcement-icon.urgent {
  background: #fecaca;
  color: #dc2626;
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.announcement-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.announcement-time {
  font-size: 12px;
  color: #9ca3af;
}

.announcement-arrow {
  color: #d1d5db;
  font-size: 20px;
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
    gap: 40px;
    text-align: center;
  }
  
  .hero-stats {
    justify-content: center;
  }
  
  .quick-actions {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 24px 16px;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-stats {
    flex-direction: column;
    align-items: center;
  }
  
  .metrics-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
