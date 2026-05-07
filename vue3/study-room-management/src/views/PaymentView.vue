<template>
  <div class="payment-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Money /></el-icon>
        支付中心
      </h2>
      <p class="page-description">管理您的余额和支付记录</p>
    </div>

    <div class="payment-grid">
      <!-- 余额信息 -->
      <div class="balance-card">
        <h3 class="card-title">我的余额</h3>
        <div class="balance-info">
          <div class="balance-amount">
            <span class="currency">¥</span>
            <span class="amount">{{ userBalance.balance || '0.00' }}</span>
          </div>
          <div class="balance-frozen" v-if="userBalance.frozenBalance > 0">
            冻结金额: ¥{{ userBalance.frozenBalance }}
          </div>
        </div>
        <el-button type="primary" @click="showRechargeDialog = true" class="recharge-btn">
          <el-icon><Plus /></el-icon>
          立即充值
        </el-button>
      </div>

      <!-- 充值记录 -->
      <div class="records-card">
        <h3 class="card-title">充值记录</h3>
        <el-table :data="rechargeRecords" style="width: 100%">
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="scope">
              <span class="amount">¥{{ scope.row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" width="100">
            <template #default="scope">
              <el-tag :type="getPaymentMethodType(scope.row.paymentMethod)">
                {{ getPaymentMethodText(scope.row.paymentMethod) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.paymentStatus)">
                {{ getStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间">
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 支付记录 -->
      <div class="records-card">
        <h3 class="card-title">支付记录</h3>
        <el-table :data="paymentRecords" style="width: 100%">
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="scope">
              <span class="amount">¥{{ scope.row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" width="100">
            <template #default="scope">
              <el-tag :type="getPaymentMethodType(scope.row.paymentMethod)">
                {{ getPaymentMethodText(scope.row.paymentMethod) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paymentTime" label="支付时间">
            <template #default="scope">
              {{ formatTime(scope.row.paymentTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog v-model="showRechargeDialog" title="账户充值" width="400px">
      <el-form :model="rechargeForm" label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="1" 
            :step="10"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="ALIPAY">支付宝</el-radio>
            <el-radio label="WECHAT">微信支付</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="recharging">
          立即充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, Plus } from '@element-plus/icons-vue'
import { paymentApi } from '../api'
import dayjs from 'dayjs'

const userBalance = ref({})
const rechargeRecords = ref([])
const paymentRecords = ref([])
const showRechargeDialog = ref(false)
const recharging = ref(false)

const rechargeForm = ref({
  amount: 10,
  paymentMethod: 'ALIPAY'
})

const getUserBalance = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const data = await paymentApi.getUserBalance(userInfo.id)
    userBalance.value = data
  } catch (error) {
    console.error('获取余额失败:', error)
  }
}

const getRechargeRecords = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const data = await paymentApi.getRechargeRecords(userInfo.id)
    rechargeRecords.value = data
  } catch (error) {
    console.error('获取充值记录失败:', error)
  }
}

const getPaymentRecords = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const data = await paymentApi.getUserPayments(userInfo.id)
    paymentRecords.value = data
  } catch (error) {
    console.error('获取支付记录失败:', error)
  }
}

const handleRecharge = async () => {
  recharging.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    await paymentApi.recharge(userInfo.id, rechargeForm.value.amount, rechargeForm.value.paymentMethod)
    ElMessage.success('充值请求已提交')
    showRechargeDialog.value = false
    // 刷新数据
    await getUserBalance()
    await getRechargeRecords()
  } catch (error) {
    ElMessage.error('充值失败')
  } finally {
    recharging.value = false
  }
}

const getPaymentMethodType = (method) => {
  const types = {
    'ALIPAY': 'primary',
    'WECHAT': 'success',
    'BALANCE': 'warning'
  }
  return types[method] || 'info'
}

const getPaymentMethodText = (method) => {
  const texts = {
    'ALIPAY': '支付宝',
    'WECHAT': '微信',
    'BALANCE': '余额'
  }
  return texts[method] || method
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'REFUNDED': 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待支付',
    'SUCCESS': '已支付',
    'FAILED': '支付失败',
    'REFUNDED': '已退款'
  }
  return texts[status] || status
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(() => {
  getUserBalance()
  getRechargeRecords()
  getPaymentRecords()
})
</script>

<style scoped>
.payment-page {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title .el-icon {
  font-size: 32px;
  color: var(--primary-color);
}

.page-description {
  color: var(--text-secondary);
  font-size: 14px;
}

.payment-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.balance-card, .records-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
}

.balance-info {
  margin-bottom: 20px;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.currency {
  font-size: 20px;
  color: var(--primary-color);
  font-weight: 600;
  margin-right: 4px;
}

.amount {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
}

.balance-frozen {
  font-size: 14px;
  color: #f56c6c;
}

.recharge-btn {
  width: 100%;
}

@media (min-width: 768px) {
  .payment-grid {
    grid-template-columns: 1fr 2fr;
  }
  
  .balance-card {
    grid-row: span 2;
  }
}
</style>
