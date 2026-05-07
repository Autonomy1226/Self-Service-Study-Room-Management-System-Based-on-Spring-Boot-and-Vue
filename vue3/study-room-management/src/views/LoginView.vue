<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon><Reading /></el-icon>
        </div>
        <h1>自习室预约系统</h1>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
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
        
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-btn"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">
          立即注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading } from '@element-plus/icons-vue'
import { userApi } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const loginData = {
      username: form.username,
      password: form.password
    }
    
    console.log('登录请求数据:', loginData)
    const response = await userApi.login(loginData)
    console.log('登录响应数据:', response)
    
    // 检查响应数据的结构
    if (response) {
      // 构造用户信息对象
      const userInfo = {
        id: response.id,
        username: response.username,
        email: response.email,
        phone: response.phone,
        role: response.role || 'USER', // 确保有默认角色
        createTime: response.createTime,
        updateTime: response.updateTime
      }
      
      console.log('保存的用户信息:', userInfo)
      
      // 存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      // 触发登录状态更新事件
      window.dispatchEvent(new CustomEvent('login-state-change', {
        detail: { userInfo }
      }))
      
      ElMessage.success('登录成功')
      
      // 管理员进入后台主界面，普通用户进入首页
      const role = String(userInfo.role || '').toUpperCase()
      router.replace(['ADMIN', 'ROLE_ADMIN', '1'].includes(role) ? '/dashboard' : '/')
    } else {
      throw new Error('登录响应数据格式错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response) {
      // 服务器返回错误
      ElMessage.error(error.response.data?.message || '登录失败，请检查用户名和密码')
    } else if (error.request) {
      // 请求发出但没有收到响应
      ElMessage.error('无法连接到服务器，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error(error.message || '登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 36px;
  border: none;
  box-shadow: none;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 64px;
  height: 64px;
  background: #eff6ff;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.logo .el-icon {
  font-size: 32px;
  color: #2563eb;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.login-header p {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e5e7eb;
  border-radius: 12px;
  padding: 4px 16px;
  transition: all 0.2s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #3b82f6;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #3b82f6;
}

.login-form :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
}

.login-form :deep(.el-input__prefix) {
  color: #9ca3af;
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  margin-top: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  transition: all 0.2s ease;
}

.login-btn:hover {
  transform: none;
  box-shadow: none;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
}

.login-footer span {
  color: #6b7280;
  font-size: 14px;
}

.register-link {
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s ease;
}

.register-link:hover {
  color: #1d4ed8;
}

/* 响应式 */
@media screen and (max-width: 480px) {
  .login-container {
    padding: 40px 24px;
    border-radius: 16px;
  }
  
  .login-header h1 {
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