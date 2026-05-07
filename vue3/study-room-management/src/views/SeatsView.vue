<template>
  <div class="seats-page">
    <div class="page-header">
      <h1>座位管理</h1>
      <p>管理自习室座位状态和添加新座位</p>
    </div>

    <div class="content-card">
      <div class="seat-controls">
        <el-select v-model="selectedRoom" placeholder="选择自习室" size="large" class="room-select">
          <el-option 
            v-for="room in rooms" 
            :key="room.id" 
            :label="room.name"
            :value="room.id" />
        </el-select>
        
        <el-button type="primary" @click="handleRefresh" size="large">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
        
        <el-button 
          v-if="selectedRoom && isAdmin" 
          type="success" 
          @click="openAddDialog"
          size="large">
          <el-icon><Plus /></el-icon>
          添加座位
        </el-button>
      </div>
      
      <div class="seat-grid" v-if="selectedRoom">
        <div v-for="seat in seats" :key="seat.id" 
             :class="['seat-item', seat.status.toLowerCase()]"
             @click="openSeatDialog(seat)">
          <span class="seat-number">{{ seat.number }}</span>
          <el-tag :type="getStatusType(seat.status)" size="small" effect="light">
            {{ seat.statusText }}
          </el-tag>
          <div class="seat-actions" v-if="isAdmin" @click.stop>
            <el-button type="info" size="small" @click="openSeatDialog(seat)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="openDeleteDialog(seat)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-if="!selectedRoom" class="empty-state">
        <el-icon><OfficeBuilding /></el-icon>
        <p>请选择自习室查看座位</p>
      </div>
    </div>

    <!-- 座位状态管理对话框 -->
    <el-dialog v-model="showDialog" title="座位状态管理" width="400px">
      <div class="seat-info" v-if="selectedSeat">
        <p><strong>座位号：</strong>{{ selectedSeat?.number }}</p>
        <p><strong>当前状态：</strong>{{ selectedSeat?.statusText }}</p>
      </div>
      <div class="status-actions">
        <el-button 
          v-for="status in seatStatuses" 
          :key="status.value"
          :type="selectedSeat?.status === status.value ? 'primary' : ''"
          @click="updateSeatStatus(status.value)">
          {{ status.label }}
        </el-button>
      </div>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="confirmStatusChange">确认</el-button>
      </template>
    </el-dialog>

    <!-- 删除座位确认对话框 -->
    <el-dialog v-model="showDeleteDialog" title="删除座位" width="400px">
      <div class="delete-warning" v-if="selectedSeat">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p><strong>确认删除座位 {{ selectedSeat?.number }}？</strong></p>
        <p class="warning-text">删除后将无法恢复，请确认该座位没有正在进行的预约。</p>
      </div>
      <template #footer>
        <el-button @click="closeDeleteDialog">取消</el-button>
        <el-button type="danger" @click="confirmDeleteSeat" :loading="deleting">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 添加座位对话框 -->
    <el-dialog v-model="showAddDialog" title="添加座位" width="400px">
      <el-form label-width="120px">
        <el-form-item label="座位号">
          <el-input 
            v-model="newSeat.seatNumber" 
            placeholder="请输入座位号"
            size="large" />
        </el-form-item>
        <el-form-item label="价格（元/小时）">
          <el-input-number 
            v-model="newSeat.price" 
            :min="0" 
            :step="0.1"
            size="large"
            style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeAddDialog">取消</el-button>
        <el-button type="primary" @click="confirmAddSeat" :disabled="!isValidNewSeat">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Plus, OfficeBuilding, Edit, Delete, WarningFilled } from '@element-plus/icons-vue'
import { seatApi, studyRoomApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedRoom = ref('')
const showDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedSeat = ref(null)
const loading = ref(false)
const deleting = ref(false)

const rooms = ref([])
const seats = ref([])

const seatStatuses = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '不可用', value: 'UNAVAILABLE' }
]

const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return user.role === 'ADMIN'
})

const getStatusType = (status) => {
  const types = {
    'AVAILABLE': 'success',
    'UNAVAILABLE': 'danger'
  }
  return types[status] || 'info'
}

// 获取自习室列表
const fetchRooms = async () => {
  try {
    const data = await studyRoomApi.getAllRooms()
    rooms.value = data.map(room => ({
      id: room.id,
      name: room.roomName
    }))
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取座位列表
const fetchSeats = async () => {
  if (!selectedRoom.value) return
  
  try {
    loading.value = true
    const data = await seatApi.getSeatsByRoom(selectedRoom.value)
    seats.value = data.map(seat => {
      const status = seatStatuses.find(s => s.value === seat.status)
      return {
        ...seat,
        number: seat.seatNumber,
        statusText: status ? status.label : '未知'
      }
    })
  } catch (error) {
    console.error('获取座位列表失败:', error)
    ElMessage.error('获取座位列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  if (selectedRoom.value) {
    fetchSeats()
  }
}

const openSeatDialog = (seat) => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  selectedSeat.value = { ...seat }
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
  selectedSeat.value = null
}

const updateSeatStatus = (status) => {
  if (selectedSeat.value) {
    selectedSeat.value.status = status
    const statusInfo = seatStatuses.find(s => s.value === status)
    selectedSeat.value.statusText = statusInfo ? statusInfo.label : '未知'
  }
}

const confirmStatusChange = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  try {
    await seatApi.updateSeatStatus(selectedSeat.value.id, selectedSeat.value.status)
    ElMessage.success('更新成功')
    fetchSeats()
    closeDialog()
  } catch (error) {
    console.error('更新座位状态失败:', error)
    ElMessage.error('更新座位状态失败')
  }
}

// 添加座位相关的响应式数据
const showAddDialog = ref(false)
const newSeat = ref({
  seatNumber: '',
  price: 0
})

// 验证新座位数据是否有效
const isValidNewSeat = computed(() => {
  return newSeat.value.seatNumber && 
         newSeat.value.price > 0 && 
         !seats.value.some(seat => seat.seatNumber === newSeat.value.seatNumber)
})

// 打开添加座位对话框
const openAddDialog = () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  newSeat.value = {
    seatNumber: '',
    price: 0
  }
  showAddDialog.value = true
}

// 关闭添加座位对话框
const closeAddDialog = () => {
  showAddDialog.value = false
  newSeat.value = {
    seatNumber: '',
    price: 0
  }
}

// 确认添加座位
const confirmAddSeat = async () => {
  if (!isValidNewSeat.value) {
    ElMessage.warning('请填写有效的座位信息')
    return
  }

  try {
    const seatData = {
      roomId: selectedRoom.value,
      seatNumber: newSeat.value.seatNumber,
      price: newSeat.value.price,
      status: 'AVAILABLE'
    }

    await seatApi.createSeat(seatData)
    ElMessage.success('添加座位成功')
    closeAddDialog()
    fetchSeats()
  } catch (error) {
    console.error('添加座位失败:', error)
    ElMessage.error('添加座位失败')
  }
}

// 打开删除座位对话框
const openDeleteDialog = (seat) => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  selectedSeat.value = { ...seat }
  showDeleteDialog.value = true
}

// 关闭删除座位对话框
const closeDeleteDialog = () => {
  showDeleteDialog.value = false
  selectedSeat.value = null
}

// 确认删除座位
const confirmDeleteSeat = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }

  try {
    deleting.value = true
    await seatApi.deleteSeat(selectedSeat.value.id)
    ElMessage.success('删除座位成功')
    closeDeleteDialog()
    fetchSeats()
  } catch (error) {
    console.error('删除座位失败:', error)
    ElMessage.error('删除座位失败，请检查是否有正在进行的预约')
  } finally {
    deleting.value = false
  }
}

// 监听自习室选择变化
watch(selectedRoom, () => {
  fetchSeats()
})

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
.seats-page {
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

.seat-controls {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.room-select {
  width: 240px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.seat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.seat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.seat-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.seat-item:hover .seat-actions {
  opacity: 1;
}

.seat-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
  height: auto;
  line-height: 1.2;
}

:deep(.el-button--info) {
  background-color: #f0f9ff;
  border-color: #0ea5e9;
  color: #0ea5e9;
}

:deep(.el-button--info:hover) {
  background-color: #e0f2fe;
  border-color: #0284c7;
  color: #0284c7;
}

:deep(.el-button--danger.is-plain) {
  background-color: #fef2f2;
  border-color: #ef4444;
  color: #ef4444;
}

:deep(.el-button--danger.is-plain:hover) {
  background-color: #fee2e2;
  border-color: #dc2626;
  color: #dc2626;
}

.seat-item.available {
  border-color: #10b981;
  background: #f0fdf4;
}

.seat-item.unavailable {
  border-color: #ef4444;
  background: #fef2f2;
}

.seat-number {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.seat-info {
  margin-bottom: 24px;
}

.seat-info p {
  margin: 8px 0;
  font-size: 15px;
  color: #374151;
}

.status-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 24px 32px;
  border-bottom: 1px solid #f3f4f6;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

:deep(.el-dialog__body) {
  padding: 32px;
}

.delete-warning {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.delete-warning p {
  margin: 8px 0;
  font-size: 15px;
  color: #374151;
}

.warning-text {
  color: #6b7280 !important;
  font-size: 14px !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .seats-page {
    padding: 20px 16px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .seat-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 12px;
  }
  
  .seat-item {
    padding: 16px;
  }
}
</style>
