<template>
  <div class="dashboard-page">
    <section class="dashboard-hero">
      <div>
        <h2 class="hero-title">管理员主界面</h2>
        <p class="hero-subtitle">围绕运营管理：看指标、盯待办、做操作</p>
      </div>
      <div class="hero-actions">
        <el-button @click="goTo('/seats')">座位管理</el-button>
        <el-button @click="goTo('/analytics')">数据分析</el-button>
        <el-button type="primary" :loading="loading" @click="refreshAll">刷新数据</el-button>
      </div>
    </section>

    <section class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">开放自习室</div>
        <div class="kpi-value">{{ overview.totalRooms || 0 }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">注册用户</div>
        <div class="kpi-value">{{ overview.totalUsers || 0 }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">总预约数</div>
        <div class="kpi-value">{{ overview.totalReservations || 0 }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">今日预约</div>
        <div class="kpi-value">{{ overview.todayReservations || 0 }}</div>
      </div>
    </section>

    <section class="board-grid">
      <el-card class="panel todo-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <span>待处理事项</span>
            <el-tag type="warning" effect="plain">管理优先</el-tag>
          </div>
        </template>
        <div class="todo-list">
          <div class="todo-item">
            <span class="todo-title">待确认预约</span>
            <el-tag>{{ reservationStats.pendingCount || 0 }}</el-tag>
          </div>
          <div class="todo-item">
            <span class="todo-title">今日新增用户</span>
            <el-tag type="success">{{ userStats.newUsersToday || 0 }}</el-tag>
          </div>
          <div class="todo-item">
            <span class="todo-title">活跃用户</span>
            <el-tag type="info">{{ userStats.activeUsers || 0 }}</el-tag>
          </div>
        </div>
      </el-card>

      <el-card class="panel action-panel" shadow="never">
        <template #header>
          <div class="panel-header">
            <span>快捷管理入口</span>
          </div>
        </template>
        <div class="shortcut-grid">
          <button class="shortcut-btn" @click="goTo('/seats')">座位状态巡检</button>
          <button class="shortcut-btn" @click="goTo('/reservations')">预约记录核查</button>
          <button class="shortcut-btn" @click="goTo('/announcements')">发布公告</button>
          <button class="shortcut-btn" @click="goTo('/notifications')">消息通知管理</button>
        </div>
      </el-card>
    </section>

    <section class="charts-grid">
      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-header"><span>近7天预约趋势</span></div>
        </template>
        <div ref="trendChart" class="chart-container"></div>
      </el-card>

      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-header"><span>预约状态分布（30天）</span></div>
        </template>
        <div ref="statusChart" class="chart-container"></div>
      </el-card>

      <el-card class="panel full-width" shadow="never">
        <template #header>
          <div class="panel-header"><span>自习室使用率</span></div>
        </template>
        <div ref="roomChart" class="chart-container"></div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dashboardApi } from '../api'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

const router = useRouter()
const loading = ref(false)

const overview = ref({})
const userStats = ref({})
const reservationStats = ref({})

const trendChart = ref(null)
const statusChart = ref(null)
const roomChart = ref(null)

let trendChartInstance = null
let statusChartInstance = null
let roomChartInstance = null

const goTo = (path) => router.push(path)

const fetchOverview = async () => {
  try {
    overview.value = await dashboardApi.getSystemOverview()
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

const fetchUserStats = async () => {
  try {
    userStats.value = await dashboardApi.getUserActivityStats()
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

const fetchReservationStats = async () => {
  try {
    const endTime = dayjs().toISOString()
    const startTime = dayjs().subtract(30, 'day').toISOString()
    reservationStats.value = await dashboardApi.getReservationStats(startTime, endTime)
  } catch (error) {
    console.error('获取预约状态统计失败:', error)
    reservationStats.value = {}
  }
}

const initTrendChart = async () => {
  try {
    const endDate = dayjs()
    const startDate = endDate.subtract(6, 'day')
    const data = await dashboardApi.getDailyReservationTrend(startDate.format('YYYY-MM-DD'), endDate.format('YYYY-MM-DD'))

    const dates = data.map(item => dayjs(item.date).format('MM-DD'))
    const counts = data.map(item => item.count)

    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '3%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: counts,
        type: 'line',
        smooth: true,
        areaStyle: {},
        itemStyle: { color: '#2563eb' }
      }]
    })
  } catch (error) {
    console.error('初始化趋势图失败:', error)
  }
}

const initStatusChart = async () => {
  try {
    const stats = reservationStats.value

    statusChartInstance = echarts.init(statusChart.value)
    statusChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['42%', '68%'],
        data: [
          { value: stats.pendingCount || 0, name: '待支付' },
          { value: stats.confirmedCount || 0, name: '已预约' },
          { value: stats.inUseCount || 0, name: '使用中' },
          { value: stats.completedCount || 0, name: '已完成' },
          { value: stats.cancelledCount || 0, name: '已取消' }
        ]
      }]
    })
  } catch (error) {
    console.error('初始化状态图失败:', error)
  }
}

const initRoomChart = async () => {
  try {
    const data = await dashboardApi.getRoomUsageStats()

    roomChartInstance = echarts.init(roomChart.value)
    roomChartInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '3%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      yAxis: { type: 'category', data: data.map(item => item.roomName) },
      series: [{
        type: 'bar',
        data: data.map(item => item.usageRate),
        label: { show: true, position: 'right', formatter: '{c}%' },
        itemStyle: { color: '#3b82f6', borderRadius: [0, 4, 4, 0] }
      }]
    })
  } catch (error) {
    console.error('初始化使用率图失败:', error)
  }
}

const refreshAll = async () => {
  loading.value = true
  try {
    await fetchOverview()
    await fetchUserStats()
    await fetchReservationStats()
    trendChartInstance?.dispose()
    statusChartInstance?.dispose()
    roomChartInstance?.dispose()
    await initTrendChart()
    await initStatusChart()
    await initRoomChart()
    ElMessage.success('数据已刷新')
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  trendChartInstance?.resize()
  statusChartInstance?.resize()
  roomChartInstance?.resize()
}

onMounted(async () => {
  await fetchOverview()
  await fetchUserStats()
  await fetchReservationStats()
  await initTrendChart()
  await initStatusChart()
  await initRoomChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  statusChartInstance?.dispose()
  roomChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
}

.hero-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.hero-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.hero-actions {
  display: inline-flex;
  gap: 8px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-card {
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.kpi-label {
  font-size: 13px;
  color: #6b7280;
}

.kpi-value {
  margin-top: 8px;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: #111827;
}

.board-grid,
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.panel {
  border: 1px solid #e5e7eb;
}

.full-width {
  grid-column: 1 / -1;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #eef2f7;
  border-radius: 8px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.shortcut-btn {
  height: 40px;
  border: 1px solid #dbe5f0;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
}

.shortcut-btn:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

.chart-container {
  height: 280px;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .board-grid,
  .charts-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .kpi-grid,
  .shortcut-grid {
    grid-template-columns: 1fr;
  }
}
</style>
