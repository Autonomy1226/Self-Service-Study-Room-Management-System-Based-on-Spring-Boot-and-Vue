import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080',  // 移除 /api 前缀
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 允许跨域请求携带cookie
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 添加跨域请求头
    config.headers['Access-Control-Allow-Origin'] = 'http://localhost:5173'
    console.log('Request:', config)
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('Response:', response)
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    console.error('Response Error:', error)
    if (error.response) {
      // 服务器返回错误状态码
      console.error('Error Status:', error.response.status)
      console.error('Error Data:', error.response.data)
      ElMessage.error(error.response.data?.message || `请求失败 (${error.response.status})`)
    } else if (error.request) {
      // 请求发出但没有收到响应
      console.error('No Response:', error.request)
      ElMessage.error('无法连接到服务器，请检查网络连接')
    } else {
      // 请求配置出错
      console.error('Request Config Error:', error.message)
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

// 用户相关API
export const userApi = {
  // 注册
  register: (data) => api.post('/api/users/register', {
    username: data.username,
    password: data.password,
    email: data.email,
    phone: data.phone,
    role: 'USER'  // 默认注册为普通用户
  }),
  // 登录
  login: (data) => {
    const formData = new URLSearchParams()
    formData.append('username', data.username)
    formData.append('password', data.password)
    return api.post('/api/users/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },
  // 获取用户信息
  getUserById: (id) => api.get(`/api/users/${id}`),
  getUserByUsername: (username) => api.get(`/api/users/username/${username}`),
  getUsersByRole: (role) => api.get(`/api/users/role/${role}`),
  getAllUsers: () => api.get('/api/users'),
  // 更新用户信息
  updateUser: (id, data) => api.put(`/api/users/${id}`, data),
  updateUserStatus: (id, status) => api.put(`/api/users/${id}/status`, { status }),
  // 删除用户
  deleteUser: (id) => api.delete(`/api/users/${id}`)
}

// 自习室相关API
export const studyRoomApi = {
  // 创建自习室
  createRoom: (data) => api.post('/api/study-rooms', data),
  // 获取自习室信息
  getRoomById: (id) => api.get(`/api/study-rooms/${id}`),
  getRoomsByStatus: (status) => api.get(`/api/study-rooms/status/${status}`),
  getAllRooms: () => api.get('/api/study-rooms'),
  // 更新自习室信息
  updateRoom: (id, data) => api.put(`/api/study-rooms/${id}`, data),
  updateRoomStatus: (id, status) => api.put(`/api/study-rooms/${id}/status`, { status }),
  // 删除自习室
  deleteRoom: (id) => api.delete(`/api/study-rooms/${id}`)
}

// 座位相关API
export const seatApi = {
  // 创建座位
  createSeat: (data) => api.post('/api/seats', data),
  // 获取座位信息
  getSeatById: (id) => api.get(`/api/seats/${id}`),
  getSeatsByRoom: (roomId) => api.get(`/api/seats/room/${roomId}`),
  getSeatsByStatus: (status) => api.get(`/api/seats/status/${status}`),
  getSeatsByRoomAndStatus: (roomId, status) => api.get(`/api/seats/room/${roomId}/status/${status}`),
  getAllSeats: () => api.get('/api/seats'),
  // 更新座位信息
  updateSeat: (id, data) => api.put(`/api/seats/${id}`, data),
  updateSeatStatus: (id, status) => api.put(`/api/seats/${id}/status?status=${status}`),
  // 删除座位
  deleteSeat: (id) => api.delete(`/api/seats/${id}`)
}

// 预约相关API
export const reservationApi = {
  // 创建预约
  createReservation: (data) => api.post('/api/reservations', data),
  // 获取预约信息
  getReservationById: (id) => api.get(`/api/reservations/${id}`),
  getReservationsByUser: (userId) => api.get(`/api/reservations/user/${userId}`),
  getReservationsBySeat: (seatId) => api.get(`/api/reservations/seat/${seatId}`),
  getReservationsByStatus: (status) => api.get(`/api/reservations/status/${status}`),
  getReservationsByTimeRange: (startTime, endTime) => api.get('/api/reservations/time-range', {
    params: { startTime, endTime }
  }),
  getAllReservations: () => api.get('/api/reservations'),
  // 更新预约信息
  updateReservation: (id, data) => api.put(`/api/reservations/${id}`, data),
  updateReservationStatus: (id, status) => api.put(`/api/reservations/${id}/status`, { status }),
  // 取消预约
  cancelReservation: (id) => api.post(`/api/reservations/${id}/cancel`),
  // 删除预约
  deleteReservation: (id) => api.delete(`/api/reservations/${id}`)
}

// 使用记录相关API
export const usageRecordApi = {
  // 创建使用记录
  createUsageRecord: (data) => api.post('/api/usage-records', data),
  // 获取使用记录
  getUsageRecordById: (id) => api.get(`/api/usage-records/${id}`),
  getUsageRecordsByReservation: (reservationId) => api.get(`/api/usage-records/reservation/${reservationId}`),
  getUsageRecordsByStatus: (status) => api.get(`/api/usage-records/status/${status}`),
  getUsageRecordsByTimeRange: (startTime, endTime) => api.get('/api/usage-records/time-range', {
    params: { startTime, endTime }
  }),
  getAllUsageRecords: () => api.get('/api/usage-records'),
  // 更新使用记录
  updateUsageRecord: (id, data) => api.put(`/api/usage-records/${id}`, data),
  updateUsageRecordStatus: (id, status) => api.put(`/api/usage-records/${id}/status`, { status }),
  // 删除使用记录
  deleteUsageRecord: (id) => api.delete(`/api/usage-records/${id}`)
}

// 公告相关API
export const announcementApi = {
  // 获取有效公告
  getActiveAnnouncements: () => api.get('/api/announcements/active'),
  // 获取所有公告（管理员）
  getAllAnnouncements: () => api.get('/api/announcements'),
  getAnnouncementById: (id) => api.get(`/api/announcements/${id}`),
  // 创建公告（管理员）
  createAnnouncement: (data) => api.post('/api/announcements', data),
  // 更新公告（管理员）
  updateAnnouncement: (id, data) => api.put(`/api/announcements/${id}`, data),
  // 删除公告（管理员）
  deleteAnnouncement: (id) => api.delete(`/api/announcements/${id}`)
}

// 通知相关API
export const notificationApi = {
  // 获取用户通知
  getUserNotifications: (userId) => api.get(`/api/notifications/user/${userId}`),
  // 获取未读通知
  getUnreadNotifications: (userId) => api.get(`/api/notifications/user/${userId}/unread`),
  // 获取未读数量
  getUnreadCount: (userId) => api.get(`/api/notifications/user/${userId}/unread-count`),
  // 创建通知
  createNotification: (data) => api.post('/api/notifications', data),
  // 标记已读
  markAsRead: (id) => api.put(`/api/notifications/${id}/read`),
  // 标记全部已读
  markAllAsRead: (userId) => api.put(`/api/notifications/user/${userId}/read-all`),
  // 删除通知
  deleteNotification: (id) => api.delete(`/api/notifications/${id}`)
}

// 评价相关API
export const reviewApi = {
  // 获取座位评价
  getSeatReviews: (seatId) => api.get(`/api/reviews/seat/${seatId}`),
  // 获取用户评价
  getUserReviews: (userId) => api.get(`/api/reviews/user/${userId}`),
  getReviewById: (id) => api.get(`/api/reviews/${id}`),
  // 创建评价
  createReview: (data) => api.post('/api/reviews', data),
  // 更新评价
  updateReview: (id, data) => api.put(`/api/reviews/${id}`, data),
  // 删除评价
  deleteReview: (id) => api.delete(`/api/reviews/${id}`),
  // 获取座位平均评分
  getSeatAverageRating: (seatId) => api.get(`/api/reviews/seat/${seatId}/average`),
  // 获取座位评分统计
}

// 支付相关API
export const paymentApi = {
  // 获取用户余额
  getUserBalance: (userId) => api.get(`/api/payment/balance/${userId}`),
  // 充值
  recharge: (userId, amount, paymentMethod) => api.post('/api/payment/recharge', { userId, amount, paymentMethod }),
  // 获取用户支付记录
  getUserPayments: (userId) => api.get(`/api/payment/records/${userId}`),
  // 获取用户充值记录
  getRechargeRecords: (userId) => api.get(`/api/payment/recharge-records/${userId}`),
  // 创建支付订单
  createPayment: (userId, reservationId, amount, paymentMethod) => api.post('/api/payment/create', { userId, reservationId, amount, paymentMethod }),
  // 余额支付
  balancePayment: (userId, reservationId, amount) => api.post('/api/payment/balance-pay', { userId, reservationId, amount }),
  // 第三方支付
  createThirdPartyPayment: (paymentMethod, orderId, amount, subject, description) => 
    api.post(`/api/third-party-payment/create/${paymentMethod}`, null, {
      params: { orderId, amount, subject, description }
    }),
  queryThirdPartyPayment: (paymentMethod, orderId) => 
    api.get(`/api/third-party-payment/query/${paymentMethod}`, {
      params: { orderId }
    })
}

// 数据可视化仪表盘API
export const dashboardApi = {
  // 获取预约统计
  getReservationStats: (startTime, endTime) => api.get('/api/dashboard/reservation-stats', {
    params: { startTime, endTime }
  }),
  // 获取自习室使用率
  getRoomUsageStats: () => api.get('/api/dashboard/room-usage'),
  // 获取每日预约趋势
  getDailyReservationTrend: (startDate, endDate) => api.get('/api/dashboard/daily-trend', {
    params: { startDate, endDate }
  }),
  // 获取座位热度排行
  getSeatPopularityRanking: (limit = 10) => api.get('/api/dashboard/seat-popularity', {
    params: { limit }
  }),
  // 获取用户活跃度统计
  getUserActivityStats: () => api.get('/api/dashboard/user-activity'),
  // 获取每日用户统计
  getDailyUserStats: (startDate, endDate) => api.get('/api/dashboard/daily-user-stats', {
    params: { startDate, endDate }
  }),
  // 获取小时统计数据
  getHourlyStats: (startTime, endTime) => api.get('/api/dashboard/hourly-stats', {
    params: { startTime, endTime }
  }),
  // 获取收入统计
  getRevenueStats: (startDate, endDate) => api.get('/api/dashboard/revenue-stats', {
    params: { startDate, endDate }
  }),
  // 获取每日收入趋势
  getDailyRevenue: (startDate, endDate) => api.get('/api/dashboard/daily-revenue', {
    params: { startDate, endDate }
  }),
  // 获取支付方式统计
  getPaymentMethodStats: (startDate, endDate) => api.get('/api/dashboard/payment-method-stats', {
    params: { startDate, endDate }
  }),
  // 获取系统概览
  getSystemOverview: () => api.get('/api/dashboard/overview')
}

export default {
  userApi,
  studyRoomApi,
  seatApi,
  reservationApi,
  usageRecordApi,
  announcementApi,
  notificationApi,
  reviewApi,
  paymentApi,
  dashboardApi
}