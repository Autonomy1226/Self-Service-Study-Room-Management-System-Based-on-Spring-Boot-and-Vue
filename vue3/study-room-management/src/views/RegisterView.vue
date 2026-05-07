<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <div class="logo">
          <el-icon><Reading /></el-icon>
        </div>
        <h1>创建账号</h1>
        <p>填写以下信息开始您的学习之旅</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            :prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="register-btn"
            size="large"
          >
            创建账号
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">
          立即登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone, Reading } from '@element-plus/icons-vue'
import { userApi } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const { confirmPassword, ...registerData } = form
    await userApi.register(registerData)
    
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 460px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 36px;
  border: none;
  box-shadow: none;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 64px;
  height: 64px;
  background: #ecfdf5;
  border-radius: 12px;
  border: 1px solid #d1fae5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.logo .el-icon {
  font-size: 32px;
  color: #059669;
}

.register-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.register-header p {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e5e7eb;
  border-radius: 12px;
  padding: 4px 16px;
  transition: all 0.2s ease;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #3b82f6;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #3b82f6;
}

.register-form :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
}

.register-form :deep(.el-input__prefix) {
  color: #9ca3af;
}

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  margin-top: 8px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  transition: all 0.2s ease;
}

.register-btn:hover {
  transform: none;
  box-shadow: none;
}

.register-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.register-footer span {
  color: #6b7280;
  font-size: 14px;
}

.login-link {
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #1d4ed8;
}

/* 响应式 */
@media screen and (max-width: 480px) {
  .register-container {
    padding: 40px 24px;
    border-radius: 16px;
  }
  
  .register-header h1 {
    font-size: 24px;
  }
  
  .logo {
    width: 64px;
    height: 64px;
  }
  
  .logo .el-icon {
    font-size: 30px;
  }
}
</style>