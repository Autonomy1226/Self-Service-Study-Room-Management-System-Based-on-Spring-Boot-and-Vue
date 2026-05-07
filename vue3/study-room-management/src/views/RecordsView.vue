<template>
  <div class="records-page">
    <div class="page-header">
      <h1>预约记录</h1>
      <p>查看和管理您的预约历史</p>
    </div>

    <div class="content-card">
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filter" class="filter-form">
          <el-form-item label="筛选条件">
            <el-select v-model="filter.type" placeholder="选择筛选类型" style="width: 120px" size="large">
              <el-option label="日期" value="date" />
              <el-option label="座位号" value="seatNumber" />
              <el-option v-if="isAdmin" label="用户名" value="username" />
            </el-select>

            <el-input
              v-if="filter.type === 'username' && isAdmin"
              v-model="filter.username"
              placeholder="输入用户名"
              clearable
              style="width: 200px"
              size="large"
            />

            <el-date-picker
              v-if="filter.type === 'date'"
              v-model="filter.date"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 200px"
              size="large"
            />

            <el-input
              v-if="filter.type === 'seatNumber'"
              v-model="filter.seatNumber"
              placeholder="输入座位号"
              clearable
              style="width: 200px"
              size="large"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch" size="large">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilter" size="large">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 预约记录列表 -->
      <el-table :data="records" v-loading="loading" class="data-table">
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="username" label="用户" width="120" v-if="isAdmin" />
        <el-table-column prop="seatNumber" label="座位号" width="100" />
        <el-table-column label="时间段" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime, 'MM-DD HH:mm') }} - {{ formatTime(row.endTime, 'HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="使用时长" width="120">
          <template #default="{ row }">
            {{ calculateDuration(row.startTime, row.endTime) }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="price" label="费用" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status, row.endTime)" effect="light" size="small">
              {{ getStatusText(row.status, row.endTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime, 'YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              plain
              @click="handlePayReservation(row)"
            >
              去支付
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              type="danger"
              size="small"
              plain
              @click="handleCancelReservation(row)"
            >
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Close } from '@element-plus/icons-vue'
import { reservationApi, userApi, seatApi, paymentApi } from '../api'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const records = ref([])

const filter = reactive({
  type: '',
  date: '',
  seatNumber: '',
  username: ''
})

const currentUser = computed(() => {
  const user = localStorage.getItem('userInfo')
  return user ? JSON.parse(user) : null
})

const isAdmin = computed(() => {
  return currentUser.value?.role === 'ADMIN'
})

const formatTime = (time, format = 'YYYY-MM-DD HH:mm') => {
  return time ? dayjs(time).format(format) : '-'
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return 0
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  return end.diff(start, 'minute')
}

const getStatusType = (status, startTime, endTime) => {
  const now = dayjs()
  const end = dayjs(endTime)
  
  if (status === 'CANCELLED') {
    return 'danger'
  }
  if (status === 'COMPLETED' || now.isAfter(end)) {
    return 'info'
  }
  if (status === 'IN_USE') {
    return 'success'
  }
  if (status === 'CONFIRMED') {
    return 'success'
  }
  if (status === 'PENDING') {
    return 'warning'
  }
  return 'info'
}

const getStatusText = (status, startTime, endTime) => {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  
  if (status === 'CANCELLED') {
    return '已取消'
  }
  if (status === 'COMPLETED' || now.isAfter(end)) {
    return '已结束'
  }
  if (status === 'IN_USE') {
    return '使用中'
  }
  if (status === 'CONFIRMED') {
    return '已预约'
  }
  if (status === 'PENDING') {
    return '待支付'
  }
  return '未知状态'
}

const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRecords()
}

const resetFilter = () => {
  filter.type = ''
  filter.date = ''
  filter.seatNumber = ''
  filter.username = ''
  currentPage.value = 1
  fetchRecords()
}

const fetchRecords = async () => {
  loading.value = true
  try {
    let data
    
    // 根据角色获取不同的预约记录
    if (isAdmin.value) {
      // 管理员获取所有预约
      data = await reservationApi.getAllReservations()
    } else if (currentUser.value) {
      // 普通用户只获取自己的预约
      data = await reservationApi.getReservationsByUser(currentUser.value.id)
    } else {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    // 前端筛选（如果设置了筛选条件）
    let filteredData = data
    if (filter.type === 'date' && filter.date) {
      filteredData = filteredData.filter(r => r.startTime?.startsWith(filter.date))
    }
    
    // 管理员可以按用户名筛选
    if (isAdmin.value && filter.type === 'username' && filter.username) {
      filteredData = filteredData.filter(r => 
        r.username?.toLowerCase().includes(filter.username.toLowerCase())
      )
    }
    
    const processedRecords = await Promise.all(filteredData.map(async (record) => {
      try {
        const seatInfo = await seatApi.getSeatById(record.seatId)
        const userInfo = await userApi.getUserById(record.userId)
        
        return {
          ...record,
          seatNumber: seatInfo.seatNumber || `座位${record.seatId}`,
          username: userInfo?.username || '未知用户'
        }
      } catch (error) {
        return {
          ...record,
          seatNumber: `座位${record.seatId}`,
          username: '未知用户'
        }
      }
    }))
    
    records.value = processedRecords
    total.value = records.value.length
  } catch (error) {
    console.error('获取预约记录失败:', error)
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!filter.type) {
    ElMessage.warning('请选择筛选类型')
    return
  }

  const value = filter[filter.type]
  if (!value) {
    ElMessage.warning('请输入筛选内容')
    return
  }

  fetchRecords()
}

const handlePayReservation = async (row) => {
  try {
    const balanceData = await paymentApi.getUserBalance(row.userId)
    const balance = balanceData?.balance || 0
    if (balance < row.totalPrice) {
      ElMessage.warning('余额不足，请先充值')
      router.push('/payment')
      return
    }
    await ElMessageBox.confirm(`确认使用余额支付 ¥${row.totalPrice} 吗？`, '余额支付', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    const payResult = await paymentApi.balancePayment(row.userId, row.id, row.totalPrice)
    if (payResult) {
      ElMessage.success('支付成功')
      fetchRecords()
    } else {
      ElMessage.error('支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleCancelReservation = async (row) => {
  const now = dayjs()
  const startTime = dayjs(row.startTime)
  
  if (now.isAfter(startTime)) {
    ElMessage.warning('预约已开始，无法取消')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '取消预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await reservationApi.cancelReservation(row.id)
    ElMessage.success('预约已取消')
    fetchRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }
}

onMounted(() => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchRecords()
})
</script>

<style scoped>
.records-page {
  padding: 40px;
  background: #fafafa;
  min-height: 100vh;
}

.page-header {
  max-width: 1400px;
  margin: 0 auto 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.page-header p {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.content-card {
  max-width: 1400px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.filter-section {
  margin-bottom: 24px;
  padding: 24px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  font-weight: 600;
  color: #374151;
  background-color: #f9fafb !important;
  padding: 16px;
}

:deep(.el-table td) {
  color: #4b5563;
  padding: 16px;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: #f3f4f6;
}

:deep(.el-tag) {
  padding: 6px 12px;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

.price {
  color: #059669;
  font-weight: 600;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .records-page {
    padding: 20px 16px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .filter-section {
    padding: 16px;
  }
}
</style>
