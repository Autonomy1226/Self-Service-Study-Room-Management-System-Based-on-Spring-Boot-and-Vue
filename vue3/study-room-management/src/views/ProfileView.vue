<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header-section">
          <h3>个人信息</h3>
        </div>
        
        <div class="profile-content">
          <div class="avatar-section">
            <div class="avatar">
              <img :src="userInfo.avatar || '/default-avatar.png'" alt="用户头像">
            </div>
            <button class="btn-upload">
              <el-icon><Camera /></el-icon>
              更换头像
            </button>
          </div>
          
          <div class="info-section">
            <div class="form-group">
              <label>用户名</label>
              <el-input v-model="userInfo.username" placeholder="请输入用户名" size="large" />
            </div>
            
            <div class="form-group">
              <label>邮箱</label>
              <el-input v-model="userInfo.email" type="email" placeholder="请输入邮箱" size="large" />
            </div>
            
            <div class="form-group">
              <label>手机号</label>
              <el-input v-model="userInfo.phone" placeholder="请输入手机号" size="large" />
            </div>
            
            <button class="btn-save" @click="saveProfile">
              <el-icon><Check /></el-icon>
              保存修改
            </button>
          </div>
        </div>
      </div>

      <!-- 修改密码卡片 -->
      <div class="password-card">
        <div class="card-header-section">
          <h3>修改密码</h3>
        </div>
        
        <div class="password-form">
          <div class="form-group">
            <label>当前密码</label>
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              placeholder="请输入当前密码"
              size="large"
              show-password />
          </div>
          
          <div class="form-group">
            <label>新密码</label>
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码"
              size="large"
              show-password />
          </div>
          
          <div class="form-group">
            <label>确认新密码</label>
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              size="large"
              show-password />
          </div>
          
          <button class="btn-change-password" @click="changePassword">
            <el-icon><Lock /></el-icon>
            修改密码
          </button>
        </div>
      </div>

      <!-- 使用统计卡片 -->
      <div class="stats-card">
        <div class="card-header-section">
          <h3>使用统计</h3>
        </div>
        
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon blue">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalHours }}</span>
              <span class="stat-label">总使用时长(小时)</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon green">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalReservations }}</span>
              <span class="stat-label">总预约次数</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon orange">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.completedReservations }}</span>
              <span class="stat-label">已完成预约</span>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon purple">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.cancelledReservations }}</span>
              <span class="stat-label">已取消预约</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Check, Lock, Clock, Calendar, Close } from '@element-plus/icons-vue'
import { userApi, usageRecordApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()

// 检查登录状态
const checkLoginStatus = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  if (!userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const userInfo = ref({
  username: '',
  email: '',
  phone: '',
  avatar: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = ref({
  totalHours: 0,
  totalReservations: 0,
  completedReservations: 0,
  cancelledReservations: 0
})

// 获取用户信息
const fetchUserInfo = async () => {
  if (!checkLoginStatus()) return
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const data = JSON.parse(userInfoStr)
      userInfo.value = data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取使用统计
const fetchStats = async () => {
  if (!checkLoginStatus()) return
  try {
    const records = await usageRecordApi.getUsageRecordsByUser(userInfo.value.username)
    const completed = records.filter(r => r.status === 'completed')
    const cancelled = records.filter(r => r.status === 'cancelled')
    
    stats.value = {
      totalHours: Math.floor(records.reduce((sum, r) => sum + r.duration, 0) / 60),
      totalReservations: records.length,
      completedReservations: completed.length,
      cancelledReservations: cancelled.length
    }
  } catch (error) {
    console.error('获取使用统计失败:', error)
  }
}

const saveProfile = async () => {
  if (!checkLoginStatus()) return
  try {
    await userApi.updateUser(userInfo.value.id, userInfo.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error(error.message || '保存失败，请稍后重试')
  }
}

const changePassword = async () => {
  if (!checkLoginStatus()) return
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await userApi.updateUser(userInfo.value.id, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改失败，请稍后重试')
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.profile-page {
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

.profile-container {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.profile-card, .password-card, .stats-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header-section {
  margin-bottom: 24px;
}

.card-header-section h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.avatar-section {
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-upload {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-upload:hover {
  background: #e5e7eb;
}

.info-section, .password-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.btn-save, .btn-change-password {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
  transition: all 0.2s ease;
}

.btn-save:hover, .btn-change-password:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background: #f3f4f6;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
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

.stat-icon.purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
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
  color: #6b7280;
  margin-top: 4px;
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

/* 响应式 */
@media (max-width: 768px) {
  .profile-page {
    padding: 20px 16px;
  }
  
  .profile-card, .password-card, .stats-card {
    padding: 20px;
  }
  
  .profile-container {
    grid-template-columns: 1fr;
  }
}
</style>
