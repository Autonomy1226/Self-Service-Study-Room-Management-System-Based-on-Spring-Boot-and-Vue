<template>
  <div class="third-party-payment">
    <div class="payment-header">
      <h2>第三方支付</h2>
      <p>请选择支付方式完成支付</p>
    </div>

    <div class="payment-content">
      <!-- 订单信息 -->
      <div class="order-info">
        <h3>订单信息</h3>
        <div class="info-item">
          <span>订单号：</span>
          <span>{{ orderId }}</span>
        </div>
        <div class="info-item">
          <span>商品名称：</span>
          <span>{{ subject }}</span>
        </div>
        <div class="info-item">
          <span>商品描述：</span>
          <span>{{ description }}</span>
        </div>
        <div class="info-item amount">
          <span>支付金额：</span>
          <span class="amount">¥{{ amount }}</span>
        </div>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <div class="payment-options">
          <div 
            class="payment-option" 
            :class="{ active: selectedMethod === 'alipay' }"
            @click="selectPaymentMethod('alipay')"
          >
            <span>支付宝</span>
          </div>
          <div 
            class="payment-option" 
            :class="{ active: selectedMethod === 'wechat' }"
            @click="selectPaymentMethod('wechat')"
          >
            <span>微信支付</span>
          </div>
        </div>
      </div>

      <!-- 支付二维码 -->
      <div class="payment-qr" v-if="qrCode">
        <h3>扫码支付</h3>
        <div class="qr-container">
          <img :src="qrCode" alt="支付二维码" class="qr-code" />
          <p>请使用{{ selectedMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</p>
        </div>
      </div>

      <!-- 支付状态 -->
      <div class="payment-status" v-if="paymentStatus">
        <h3>支付状态</h3>
        <el-tag :type="getStatusType(paymentStatus)">
          {{ getStatusText(paymentStatus) }}
        </el-tag>
      </div>
    </div>

    <!-- 支付结果对话框 -->
    <el-dialog v-model="showResultDialog" title="支付结果" width="400px">
      <div class="result-content">
        <div class="result-icon" :class="paymentSuccess ? 'success' : 'failed'">
          <el-icon size="60">
            <CircleCheck v-if="paymentSuccess" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <h3>{{ paymentSuccess ? '支付成功' : '支付失败' }}</h3>
        <p v-if="!paymentSuccess">{{ errorMessage }}</p>
      </div>
      <template #footer>
        <el-button @click="showResultDialog = false">关闭</el-button>
        <el-button type="primary" @click="goBack" v-if="paymentSuccess">返回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { paymentApi } from '../api'

const route = useRoute()
const router = useRouter()

const orderId = ref('')
const amount = ref('')
const subject = ref('')
const description = ref('')
const selectedMethod = ref('')
const qrCode = ref('')
const paymentStatus = ref('')
const showResultDialog = ref(false)
const paymentSuccess = ref(false)
const errorMessage = ref('')

let statusTimer = null

onMounted(() => {
  // 从路由参数获取订单信息
  orderId.value = route.query.orderId || ''
  amount.value = route.query.amount || ''
  subject.value = route.query.subject || '自习室预约'
  description.value = route.query.description || ''
  
  const reservationId = route.query.reservationId
  const userId = route.query.userId
  const method = route.query.paymentMethod || 'ALIPAY'
  
  if (!orderId.value || !reservationId) {
    ElMessage.error('订单信息不完整')
    router.back()
    return
  }
  
  // 模拟支付成功（API未对接，直接创建支付记录并确认）
  simulatePaymentSuccess(userId, reservationId, method)
})

onUnmounted(() => {
  if (statusTimer) {
    clearInterval(statusTimer)
  }
})

const simulatePaymentSuccess = async (userId, reservationId, method) => {
  try {
    // 创建支付记录
    const createdPayment = await paymentApi.createPayment(
      userId,
      reservationId,
      parseFloat(amount.value),
      method
    )
    const paymentId = createdPayment?.id || createdPayment
    
    // 确认支付
    await paymentApi.confirmPayment(paymentId)
    
    paymentStatus.value = 'SUCCESS'
    paymentSuccess.value = true
    showResultDialog.value = true
  } catch (error) {
    console.error('模拟支付失败:', error)
    paymentStatus.value = 'FAILED'
    paymentSuccess.value = false
    errorMessage.value = '支付失败'
    showResultDialog.value = true
  }
}

const startStatusCheck = () => {
  statusTimer = setInterval(async () => {
    try {
      const result = await paymentApi.queryThirdPartyPayment(selectedMethod.value, orderId.value)
      paymentStatus.value = result.status
      
      if (result.status === 'SUCCESS') {
        clearInterval(statusTimer)
        showPaymentResult(true)
      } else if (result.status === 'FAILED') {
        clearInterval(statusTimer)
        showPaymentResult(false)
      }
    } catch (error) {
      console.error('查询支付状态失败:', error)
    }
  }, 3000) // 每3秒查询一次
}

const showPaymentResult = (success) => {
  paymentSuccess.value = success
  showResultDialog.value = true
}

const goBack = () => {
  router.push('/records')
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待支付',
    'SUCCESS': '支付成功',
    'FAILED': '支付失败'
  }
  return texts[status] || status
}
</script>

<style scoped>
.third-party-payment {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.payment-header {
  text-align: center;
  margin-bottom: 40px;
}

.payment-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.payment-header p {
  color: #666;
  font-size: 16px;
}

.order-info, .payment-methods, .payment-qr, .payment-status {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-info h3, .payment-methods h3, .payment-qr h3, .payment-status h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item.amount {
  font-weight: 600;
  color: #f56c6c;
}

.payment-options {
  display: flex;
  gap: 16px;
}

.payment-option {
  flex: 1;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-option:hover {
  border-color: #409eff;
}

.payment-option.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.qr-container {
  text-align: center;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
}

.result-content {
  text-align: center;
}

.result-icon {
  margin-bottom: 16px;
}

.result-icon.success {
  color: #67c23a;
}

.result-icon.failed {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .payment-options {
    flex-direction: column;
  }
  
  .qr-code {
    width: 150px;
    height: 150px;
  }
}
</style>
