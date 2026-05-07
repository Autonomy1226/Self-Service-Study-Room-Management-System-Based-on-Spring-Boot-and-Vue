<template>
  <div class="tech-dashboard-page">
    <section class="top-summary">
      <div class="summary-title">
        <h2>数据仪表盘</h2>
        <p>管理员数据总览</p>
      </div>
      <el-button type="primary" :loading="loading" @click="refreshAll">刷新数据</el-button>
    </section>

    <section class="stats-grid">
      <div class="stat-card">
        <div class="label">开放自习室</div>
        <div class="value">{{ overview.totalRooms || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">注册用户</div>
        <div class="value">{{ overview.totalUsers || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">总预约数</div>
        <div class="value">{{ overview.totalReservations || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">今日预约</div>
        <div class="value">{{ overview.todayReservations || 0 }}</div>
      </div>
    </section>

    <section class="chart-grid">
      <el-card shadow="never" class="chart-card">
        <template #header>近7天预约趋势</template>
        <div ref="trendRef" class="chart"></div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>预约状态分布（30天）</template>
        <div ref="statusRef" class="chart"></div>
      </el-card>

      <el-card shadow="never" class="chart-card full-width">
        <template #header>自习室使用率</template>
        <div ref="roomRef" class="chart"></div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dashboardApi } from '../api'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

const loading = ref(false)
const overview = ref({})
const reservationStats = ref({})

const trendRef = ref(null)
const statusRef = ref(null)
const roomRef = ref(null)

let trendChart = null
let statusChart = null
let roomChart = null

const fetchOverview = async () => {
  try {
    overview.value = await dashboardApi.getSystemOverview()
  } catch (error) {
    console.error('获取概览失败:', error)
  }
}

const fetchReservationStats = async () => {
  try {
    const end = dayjs().toISOString()
    const start = dayjs().subtract(30, 'day').toISOString()
    reservationStats.value = await dashboardApi.getReservationStats(start, end)
  } catch (error) {
    console.error('获取预约统计失败:', error)
    reservationStats.value = {}
  }
}

const initTrendChart = async () => {
  const endDate = dayjs()
  const startDate = endDate.subtract(6, 'day')
  const data = await dashboardApi.getDailyReservationTrend(startDate.format('YYYY-MM-DD'), endDate.format('YYYY-MM-DD'))

  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '3%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(i => dayjs(i.date).format('MM-DD')) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: data.map(i => i.count), itemStyle: { color: '#3b82f6' } }]
  })
}

const initStatusChart = () => {
  statusChart = echarts.init(statusRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      data: [
        { value: reservationStats.value.pendingCount || 0, name: '进行中' },
        { value: reservationStats.value.completedCount || 0, name: '已完成' },
        { value: reservationStats.value.cancelledCount || 0, name: '已取消' }
      ]
    }]
  })
}

const initRoomChart = async () => {
  const data = await dashboardApi.getRoomUsageStats()

  roomChart = echarts.init(roomRef.value)
  roomChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '3%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: data.map(i => i.roomName) },
    series: [{
      type: 'bar',
      data: data.map(i => i.usageRate),
      label: { show: true, position: 'right', formatter: '{c}%' },
      itemStyle: { color: '#60a5fa', borderRadius: [0, 4, 4, 0] }
    }]
  })
}

const disposeCharts = () => {
  trendChart?.dispose()
  statusChart?.dispose()
  roomChart?.dispose()
  trendChart = null
  statusChart = null
  roomChart = null
}

const refreshAll = async () => {
  loading.value = true
  try {
    await fetchOverview()
    await fetchReservationStats()
    disposeCharts()
    await initTrendChart()
    initStatusChart()
    await initRoomChart()
    ElMessage.success('刷新成功')
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  trendChart?.resize()
  statusChart?.resize()
  roomChart?.resize()
}

onMounted(async () => {
  await refreshAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.tech-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
}

.summary-title h2 {
  margin: 0;
  font-size: 22px;
}

.summary-title p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
}

.stat-card .label {
  color: #6b7280;
  font-size: 13px;
}

.stat-card .value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.chart-card {
  border: 1px solid #e5e7eb;
}

.full-width {
  grid-column: 1 / -1;
}

.chart {
  height: 300px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .top-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
