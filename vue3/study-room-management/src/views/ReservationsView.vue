<template>
  <div class="reservations-page">
    <div class="page-header">
      <h1>预约座位</h1>
      <p>选择自习室、日期和座位进行预约</p>
    </div>

    <div class="content-card">
      <h3 class="section-title">预约信息</h3>
      
      <el-form :model="form" label-width="100px" class="reservation-form">
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="自习室">
              <el-select 
                v-model="form.roomId" 
                placeholder="选择自习室"
                @change="handleRoomChange"
                size="large">
                <el-option
                  v-for="room in rooms"
                  :key="room.id"
                  :label="room.name"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="日期">
              <el-date-picker
                v-model="form.date"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                value-format="YYYY-MM-DD"
                @change="handleDateChange"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="开始时间">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择开始时间"
                format="HH:mm"
                :disabled-hours="disabledHours"
                :disabled-minutes="disabledMinutes"
                @change="handleTimeChange"
                size="large"
              />
            </el-form-item>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="结束时间">
              <el-time-picker
                v-model="form.endTime"
                placeholder="选择结束时间"
                format="HH:mm"
                :disabled-hours="disabledEndHours"
                :disabled-minutes="disabledEndMinutes"
                @change="handleTimeChange"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 座位选择区域 -->
        <div v-if="form.roomId && form.date && form.startTime && form.endTime" class="seat-selection">
          <h4 class="subsection-title">选择座位</h4>
          <div v-if="floorPlanUrl && seatsHavePos" class="floorplan-wrapper">
            <div class="floorplan-stage">
              <img class="floorplan-image" :src="floorPlanUrl" alt="自习室平面图" />
              <button
                v-for="seat in availableSeats"
                :key="seat.id"
                class="seat-pin"
                :class="{
                  selected: selectedSeat?.id === seat.id,
                  reserved: seat.status === 'reserved'
                }"
                :style="seatPinStyle(seat)"
                @click="selectSeat(seat)"
                :disabled="seat.status === 'reserved'"
                type="button"
              >
                {{ seat.number }}
              </button>
            </div>
            <div class="floorplan-legend">
              <div class="legend-item"><span class="dot available" /> 可用</div>
              <div class="legend-item"><span class="dot reserved" /> 已被预约</div>
              <div class="legend-item"><span class="dot selected" /> 已选择</div>
            </div>
          </div>
          <div v-else class="seat-grid">
            <div
              v-for="seat in availableSeats"
              :key="seat.id"
              :class="['seat-card', { 
                selected: selectedSeat?.id === seat.id,
                reserved: seat.status === 'reserved'
              }]"
              @click="selectSeat(seat)"
            >
              <div class="seat-number">{{ seat.number }}</div>
              <div class="seat-status">
                <el-tag :type="getSeatStatusType(seat.status)" size="small" effect="light">
                  {{ seat.statusText }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 预约信息确认 -->
        <div v-if="selectedSeat" class="reservation-summary">
          <h4 class="subsection-title">预约确认</h4>
          <div class="summary-content">
            <div class="summary-item">
              <span class="label">自习室：</span>
              <span class="value">{{ selectedRoom?.name }}</span>
            </div>
            <div class="summary-item">
              <span class="label">座位：</span>
              <span class="value">{{ selectedSeat.number }}</span>
            </div>
            <div class="summary-item">
              <span class="label">日期：</span>
              <span class="value">{{ form.date }}</span>
            </div>
            <div class="summary-item">
              <span class="label">时间：</span>
              <span class="value">{{ formatTime(form.startTime) }} - {{ formatTime(form.endTime) }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large"
            class="submit-btn"
            @click="handleSubmit"
            :loading="submitting">
            <el-icon><Check /></el-icon>
            确认预约
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 支付对话框 -->
    <el-dialog v-model="showPaymentDialog" title="确认支付" width="400px" :close-on-click-modal="false">
      <div class="payment-info">
        <h4>预约信息</h4>
        <div class="info-item">
          <span>座位编号：</span>
          <span>{{ paymentInfo.seatNumber }}</span>
        </div>
        <div class="info-item">
          <span>预约时间：</span>
          <span>{{ formatTime(paymentInfo.startTime) }} - {{ formatTime(paymentInfo.endTime) }}</span>
        </div>
        <div class="info-item">
          <span>预约时长：</span>
          <span>{{ calculateDuration(paymentInfo.startTime, paymentInfo.endTime) }} 分钟</span>
        </div>
        <div class="info-item total">
          <span>应付金额：</span>
          <span class="amount">¥{{ calculatePrice() }}</span>
        </div>
      </div>
      
      <el-form label-width="80px" style="margin-top: 20px;">
        <el-form-item label="支付方式">
          <el-radio-group v-model="paymentMethod">
            <el-radio value="BALANCE">
              余额支付 (当前余额: ¥{{ userBalance }})
            </el-radio>
            <el-radio value="ALIPAY">支付宝</el-radio>
            <el-radio value="WECHAT">微信支付</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="cancelPayment">取消</el-button>
        <el-button type="primary" @click="handlePayment" :loading="paying">
          确认支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { reservationApi, studyRoomApi, seatApi, paymentApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const rooms = ref([])
const availableSeats = ref([])
const selectedSeat = ref(null)
const floorPlanUrl = ref('')

// 支付相关
const showPaymentDialog = ref(false)
const currentReservation = ref(null)
const userBalance = ref(0)
const paymentMethod = ref('BALANCE')
const paying = ref(false)

// 保存预约信息用于支付对话框
const paymentInfo = ref({
  seatNumber: '',
  startTime: null,
  endTime: null,
  date: ''
})

const form = ref({
  roomId: '',
  date: '',
  startTime: null,
  endTime: null
})

const selectedRoom = computed(() => {
  return rooms.value.find(room => room.id === form.value.roomId)
})

const seatsHavePos = computed(() => {
  return availableSeats.value.some(s => Number.isFinite(s.posX) && Number.isFinite(s.posY))
})

const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const disabledHours = () => {
  return [0, 1, 2, 3, 4, 5, 6, 22, 23]
}

const disabledMinutes = () => {
  return []
}

const disabledEndHours = () => {
  if (!form.value.startTime) return [0, 1, 2, 3, 4, 5, 6, 22, 23]
  const startHour = form.value.startTime.getHours()
  const hours = []
  for (let i = 0; i <= startHour; i++) {
    hours.push(i)
  }
  hours.push(23)
  return hours
}

const disabledEndMinutes = (hour) => {
  if (!form.value.startTime) return []
  const startHour = form.value.startTime.getHours()
  const startMinute = form.value.startTime.getMinutes()
  
  if (hour === startHour) {
    const minutes = []
    for (let i = 0; i <= startMinute; i++) {
      minutes.push(i)
    }
    return minutes
  }
  return []
}

const getSeatStatusType = (status) => {
  const types = {
    'available': 'success',
    'reserved': 'danger',
    'maintenance': 'warning'
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('HH:mm')
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return 0
  const start = dayjs(`2000-01-01T${formatTime(startTime)}:00`)
  const end = dayjs(`2000-01-01T${formatTime(endTime)}:00`)
  return end.diff(start, 'minute')
}

const checkLoginStatus = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const fetchRooms = async () => {
  try {
    const data = await studyRoomApi.getAllRooms()
    rooms.value = data.filter(room => room.status === 'OPEN')
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  }
}

const seatPinStyle = (seat) => {
  const x = Number.isFinite(seat.posX) ? seat.posX : 0
  const y = Number.isFinite(seat.posY) ? seat.posY : 0
  return {
    left: `${x}%`,
    top: `${y}%`
  }
}

const normalizeFloorPlanUrl = (url) => {
  if (!url) return ''
  const backendOrigin = import.meta.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080'
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return `${backendOrigin}${url}`
  return `${backendOrigin}/${url}`
}

const handleRoomChange = () => {
  selectedSeat.value = null
  if (form.value.date && form.value.startTime && form.value.endTime) {
    fetchAvailableSeats()
  }
}

const handleDateChange = () => {
  selectedSeat.value = null
  if (form.value.roomId && form.value.startTime && form.value.endTime) {
    fetchAvailableSeats()
  }
}

const handleTimeChange = () => {
  selectedSeat.value = null
  if (form.value.roomId && form.value.date && form.value.startTime && form.value.endTime) {
    fetchAvailableSeats()
  }
}

const selectSeat = (seat) => {
  if (seat.status === 'reserved') {
    ElMessage.warning('该座位已被预约')
    return
  }
  selectedSeat.value = seat
}

const fetchAvailableSeats = async () => {
  if (!form.value.roomId || !form.value.date || !form.value.startTime || !form.value.endTime) {
    return
  }
  
  loading.value = true
  try {
    const room = await studyRoomApi.getRoomById(form.value.roomId)
    floorPlanUrl.value = normalizeFloorPlanUrl(room?.floorPlanUrl || '')

    const seats = await seatApi.getSeatsByRoom(form.value.roomId)
    
    const startTime = `${form.value.date}T${formatTime(form.value.startTime)}:00`
    const endTime = `${form.value.date}T${formatTime(form.value.endTime)}:00`
    
    const dayStart = `${form.value.date}T00:00:00`
    const dayEnd = `${form.value.date}T23:59:59`
    const reservations = await reservationApi.getReservationsByTimeRange(dayStart, dayEnd)
    
    availableSeats.value = seats.map(seat => {
      const seatReservations = reservations.filter(reservation => 
        reservation.seatId === seat.id && 
        (reservation.status === 'PENDING' || reservation.status === 'CONFIRMED' || reservation.status === 'IN_USE')
      )
      
      const hasConflict = seatReservations.some(reservation => {
        const reservationStart = dayjs(reservation.startTime)
        const reservationEnd = dayjs(reservation.endTime)
        const selectedStart = dayjs(startTime)
        const selectedEnd = dayjs(endTime)
        
        // 使用与后端相同的时间重叠逻辑
        return !(selectedEnd.isBefore(reservationStart) || selectedStart.isAfter(reservationEnd))
      })
      
      return {
        id: seat.id,
        number: seat.seatNumber,
        seatNumber: seat.seatNumber,
        roomId: seat.roomId,
        price: seat.price,
        posX: seat.posX,
        posY: seat.posY,
        status: hasConflict ? 'reserved' : 'available',
        statusText: hasConflict ? '已被预约' : '可用'
      }
    })
  } catch (error) {
    console.error('获取座位列表失败:', error)
    ElMessage.error('获取座位列表失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!selectedSeat.value) {
    ElMessage.warning('请选择座位')
    return
  }
  
  // 获取当前登录用户
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  submitting.value = true
  try {
    const startTime = `${form.value.date}T${formatTime(form.value.startTime)}:00`
    const endTime = `${form.value.date}T${formatTime(form.value.endTime)}:00`
    
    // 1. 创建预约
    const reservation = await reservationApi.createReservation({
      seatId: selectedSeat.value.id,
      userId: userInfo.id,
      startTime,
      endTime
    })
    
    // 3. 保存预约信息用于支付对话框
    paymentInfo.value = {
      seatNumber: selectedSeat.value.seatNumber,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      date: form.value.date
    }
    
    // 4. 显示支付对话框
    showPaymentDialog.value = true
    currentReservation.value = reservation
    
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error(error.response?.data?.message || '预约失败')
  } finally {
    submitting.value = false
  }
}

// 支付相关方法
const loadUserBalance = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const data = await paymentApi.getUserBalance(userInfo.id)
    userBalance.value = data.balance || 0
  } catch (error) {
    console.error('获取余额失败:', error)
  }
}

const calculatePrice = () => {
  console.log('calculatePrice called')
  console.log('selectedSeat.value:', selectedSeat.value)
  console.log('paymentInfo.value:', paymentInfo.value)
  
  if (!selectedSeat.value) {
    console.log('selectedSeat is null')
    return '0.00'
  }
  
  if (!paymentInfo.value.startTime || !paymentInfo.value.endTime) {
    console.log('startTime or endTime is null')
    return '0.00'
  }
  
  const duration = calculateDuration(paymentInfo.value.startTime, paymentInfo.value.endTime)
  console.log('duration:', duration, 'minutes')
  
  const units = Math.ceil(duration / 30) // 每30分钟一个单位
  console.log('units:', units)
  
  // 修复：确保price是数字，如果是字符串则转换
  let seatPrice = selectedSeat.value.price || 0
  if (typeof seatPrice === 'string') {
    seatPrice = parseFloat(seatPrice) || 0
  }
  
  // 如果价格仍然为0或无效，使用默认价格
  if (seatPrice <= 0 || isNaN(seatPrice)) {
    // 根据房间类型设置默认价格
    const roomId = selectedSeat.value.roomId
    if (roomId === 1) seatPrice = 1.5  // 静音自习室
    else if (roomId === 2) seatPrice = 2.0  // 自习室B
    else if (roomId === 3) seatPrice = 3.0  // 讨论自习室
    else seatPrice = 2.0  // 默认价格
    console.log('Using default price:', seatPrice)
  }
  
  console.log('seatPrice:', seatPrice)
  
  const price = seatPrice * units
  console.log('calculated price:', price)
  
  return isNaN(price) ? '0.00' : price.toFixed(2)
}

const handlePayment = async () => {
  paying.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const amountStr = calculatePrice()
    const amount = parseFloat(amountStr)
    
    if (isNaN(amount) || amount <= 0) {
      ElMessage.error('金额计算错误')
      return
    }
    
    if (paymentMethod.value === 'BALANCE') {
      // 余额支付 - 先检查余额
      const balanceData = await paymentApi.getUserBalance(userInfo.id)
      const balance = balanceData?.balance || 0
      if (balance < amount) {
        ElMessage.warning('余额不足，请先充值')
        // 取消预约，不保留记录
        try {
          await reservationApi.cancelReservation(currentReservation.value.id)
        } catch (e) {
          console.error('取消预约失败:', e)
        }
        showPaymentDialog.value = false
        router.push('/payment')
        return
      }
      const payResult = await paymentApi.balancePayment(userInfo.id, currentReservation.value.id, amount)
      if (!payResult) {
        ElMessage.error('支付失败')
        try {
          await reservationApi.cancelReservation(currentReservation.value.id)
        } catch (e) {
          console.error('取消预约失败:', e)
        }
        showPaymentDialog.value = false
        return
      }
      ElMessage.success('支付成功')
      
    } else {
      // 第三方支付 - 跳转到第三方支付页面
      router.push({
        path: '/third-party-payment',
        query: {
          orderId: 'ORDER' + Date.now(),
          amount: amountStr,
          subject: '自习室预约',
          description: `座位${paymentInfo.value.seatNumber}预约`,
          paymentMethod: paymentMethod.value,
          reservationId: currentReservation.value.id,
          userId: userInfo.id
        }
      })
      return
    }
    
    // 支付成功，关闭对话框，重置表单
    showPaymentDialog.value = false
    form.value = {
      roomId: '',
      date: '',
      startTime: null,
      endTime: null
    }
    selectedSeat.value = null
    fetchAvailableSeats()
    
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.response?.data?.message || '支付失败')
    // 支付失败，取消预约，不保留任何记录
    try {
      await reservationApi.cancelReservation(currentReservation.value.id)
    } catch (e) {
      console.error('取消预约失败:', e)
    }
    showPaymentDialog.value = false
  } finally {
    paying.value = false
  }
}

const cancelPayment = async () => {
  try {
    // 取消预约
    await reservationApi.cancelReservation(currentReservation.value.id)
    ElMessage.info('预约已取消')
  } catch (error) {
    console.error('取消预约失败:', error)
  }
  
  showPaymentDialog.value = false
  currentReservation.value = null
  paymentInfo.value = {
    seatNumber: '',
    startTime: null,
    endTime: null,
    date: ''
  }
}

onMounted(() => {
  if (!checkLoginStatus()) return
  fetchRooms()
  loadUserBalance()
})
</script>

<style scoped>
.reservations-page {
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

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24px;
}

.subsection-title {
  font-size: 17px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.reservation-form {
  max-width: 1200px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e5e7eb;
  border-radius: 10px;
  padding: 4px 16px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #3b82f6;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #3b82f6;
}

:deep(.el-select) {
  width: 100%;
}

.seat-selection {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #f3f4f6;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.floorplan-wrapper {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.floorplan-stage {
  position: relative;
  width: 100%;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.floorplan-image {
  width: 100%;
  height: auto;
  display: block;
}

.seat-pin {
  position: absolute;
  transform: translate(-50%, -50%);
  min-width: 54px;
  height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: rgba(255, 255, 255, 0.92);
  color: #111827;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: all 0.15s ease;
}

.seat-pin:hover {
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.18);
}

.seat-pin.selected {
  border-color: #2563eb;
  background: rgba(37, 99, 235, 0.10);
  color: #1d4ed8;
}

.seat-pin.reserved {
  border-color: #fecaca;
  background: rgba(254, 242, 242, 0.95);
  color: #991b1b;
  cursor: not-allowed;
  opacity: 0.85;
}

.floorplan-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #fafafa;
  color: #374151;
  font-size: 13px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
}

.dot.available {
  background: #22c55e;
}

.dot.reserved {
  background: #ef4444;
}

.dot.selected {
  background: #3b82f6;
}

.seat-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.seat-card:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.seat-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.seat-card.reserved {
  border-color: #fee2e2;
  background: #fef2f2;
  cursor: not-allowed;
  opacity: 0.7;
}

.seat-number {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.seat-card.selected .seat-number {
  color: #3b82f6;
}

.reservation-summary {
  margin-top: 40px;
  padding: 32px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-item .label {
  font-size: 14px;
  color: #6b7280;
}

.summary-item .value {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.3);
}

.payment-info h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item.total {
  font-weight: 600;
  border-top: 1px solid #eee;
  padding-top: 12px;
  margin-top: 12px;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

/* 响应式 */
@media (max-width: 768px) {
  .reservations-page {
    padding: 20px 16px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .seat-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 12px;
  }

  .seat-pin {
    min-width: 46px;
    height: 30px;
    font-size: 11px;
    padding: 0 8px;
  }
  
  .seat-card {
    padding: 16px;
  }
  
  .summary-content {
    grid-template-columns: 1fr;
  }
}
</style>
