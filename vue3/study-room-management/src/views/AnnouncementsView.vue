<template>
  <div class="announcements-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Bell /></el-icon>
        公告管理
      </h2>
      <p class="page-description">管理系统公告和通知</p>
    </div>

    <div class="content-card">
      <div class="card-header">
        <h3>公告列表</h3>
        <el-button type="primary" size="large" @click="handleAdd" v-if="isAdmin">
          <el-icon><Plus /></el-icon>
          发布公告
        </el-button>
      </div>

      <el-table :data="announcements" v-loading="loading" class="data-table">
        <el-table-column type="index" width="60" label="序号" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-rate :model-value="row.priority / 2" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '有效' : '失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发布公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '发布公告' : '编辑公告'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="选择公告类型" style="width: 100%">
            <el-option label="普通公告" value="GENERAL" />
            <el-option label="规则通知" value="RULE" />
            <el-option label="维护通知" value="MAINTENANCE" />
            <el-option label="紧急通知" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-slider v-model="form.priority" :max="10" show-stops :step="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">有效</el-radio>
            <el-radio label="INACTIVE">失效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { announcementApi } from '../api'
import dayjs from 'dayjs'

const loading = ref(false)
const announcements = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 'GENERAL',
  priority: 5,
  status: 'ACTIVE'
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.role === 'ADMIN'
})

const getTypeTag = (type) => {
  const tags = {
    'GENERAL': 'info',
    'RULE': 'warning',
    'MAINTENANCE': 'danger',
    'URGENT': 'danger'
  }
  return tags[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = {
    'GENERAL': '普通公告',
    'RULE': '规则通知',
    'MAINTENANCE': '维护通知',
    'URGENT': '紧急通知'
  }
  return labels[type] || type
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const data = await announcementApi.getActiveAnnouncements()
    announcements.value = data
  } catch (error) {
    console.error('获取公告失败:', error)
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.id = null
  form.title = ''
  form.content = ''
  form.type = 'GENERAL'
  form.priority = 5
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })
    await announcementApi.deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (dialogType.value === 'add') {
      await announcementApi.createAnnouncement(form)
      ElMessage.success('发布公告成功')
    } else {
      await announcementApi.updateAnnouncement(form.id, form)
      ElMessage.success('更新公告成功')
    }
    
    dialogVisible.value = false
    fetchAnnouncements()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcements-page {
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

.content-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.data-table {
  margin-top: 20px;
}
</style>
