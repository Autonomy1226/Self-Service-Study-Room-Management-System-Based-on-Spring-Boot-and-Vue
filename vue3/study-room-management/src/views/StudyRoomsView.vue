<template>
  <div class="study-rooms-page">
    <div class="page-header">
      <h1>自习室管理</h1>
      <p>查看和管理所有自习室信息</p>
    </div>

    <div class="content-card">
      <div class="card-header-actions">
        <h3>自习室列表</h3>
        <el-button v-if="isAdmin" type="primary" @click="handleAdd" size="large">
          <el-icon><Plus /></el-icon>
          添加自习室
        </el-button>
      </div>

      <el-table :data="rooms" v-loading="loading" class="data-table">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="自习室名称" min-width="150" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="large" effect="light">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain
              @click="handleDelete(row)"
              v-if="row.status !== 'in_use'">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑自习室对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加自习室' : '编辑自习室'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="自习室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入自习室名称" />
        </el-form-item>
        
        <el-form-item label="容量" prop="capacity">
          <el-input-number 
            v-model="form.capacity" 
            :min="1" 
            :max="200"
            placeholder="请输入容量"
            style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="开放" value="OPEN" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { studyRoomApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const formRef = ref(null)
const rooms = ref([])

const form = reactive({
  id: null,
  name: '',
  capacity: 20,
  status: 'OPEN',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入自习室名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入容量', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '容量必须在1-200之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '描述不能超过 200 个字符', trigger: 'blur' }
  ]
}

const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return user.role === 'ADMIN'
})

const getStatusType = (status) => {
  const types = {
    'OPEN': 'success',
    'CLOSED': 'danger',
    'MAINTENANCE': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'OPEN': '开放',
    'CLOSED': '关闭',
    'MAINTENANCE': '维护中'
  }
  return texts[status] || status
}

const fetchRooms = async () => {
  loading.value = true
  try {
    const data = await studyRoomApi.getAllRooms()
    rooms.value = data.map(room => ({
      ...room,
      statusText: getStatusText(room.status)
    }))
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.id = null
  form.name = ''
  form.capacity = 20
  form.status = 'OPEN'
  form.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除自习室"${row.name}"？<br><br>
       <span style="color: #e74c3c; font-weight: bold;">⚠️ 注意：删除自习室将同时删除该自习室内的所有座位，此操作不可恢复！</span>`, 
      '删除自习室确认', 
      {
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
    await studyRoomApi.deleteRoom(row.id)
    ElMessage.success('删除成功')
    fetchRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除自习室失败:', error)
      ElMessage.error('删除自习室失败，请稍后重试')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      ...form,
      roomName: form.name
    }
    
    if (dialogType.value === 'add') {
      await studyRoomApi.createRoom(submitData)
      ElMessage.success('添加成功')
    } else {
      await studyRoomApi.updateRoom(form.id, submitData)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    fetchRooms()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
.study-rooms-page {
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

.card-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header-actions h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
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

/* 响应式 */
@media (max-width: 768px) {
  .study-rooms-page {
    padding: 20px 16px;
  }
  
  .content-card {
    padding: 20px;
  }
  
  .card-header-actions {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
