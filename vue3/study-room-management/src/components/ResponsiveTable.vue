<template>
  <div class="responsive-table-container">
    <!-- 桌面端表格 -->
    <el-table
      v-if="!isMobile"
      :data="tableData"
      :style="{ width: '100%' }"
      :height="height"
      :stripe="stripe"
      :border="border"
      :size="size"
      v-bind="$attrs"
    >
      <slot />
    </el-table>

    <!-- 移动端卡片列表 -->
    <div v-else class="mobile-card-list">
      <div
        v-for="(item, index) in tableData"
        :key="getRowKey(item, index)"
        class="mobile-card-item"
        @click="handleRowClick(item, index)"
      >
        <slot name="mobile-item" :item="item" :index="index">
          <div class="mobile-card-content">
            <div
              v-for="column in mobileColumns"
              :key="column.prop"
              class="mobile-field"
            >
              <span class="field-label">{{ column.label }}:</span>
              <span class="field-value">{{ formatValue(item[column.prop], column) }}</span>
            </div>
          </div>
        </slot>
        
        <div class="mobile-card-actions" v-if="$slots.actions">
          <slot name="actions" :item="item" :index="index" />
        </div>
      </div>
    </div>

    <!-- 分页器 -->
    <div class="pagination-container" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        :small="isMobile"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  tableData: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  height: {
    type: [String, Number],
    default: 'auto'
  },
  stripe: {
    type: Boolean,
    default: true
  },
  border: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  total: {
    type: Number,
    default: 0
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  }
})

const emit = defineEmits(['size-change', 'current-change', 'row-click'])

// 响应式状态
const isMobile = ref(window.innerWidth < 768)
const currentPage = ref(1)
const pageSize = ref(10)

// 计算属性
const mobileColumns = computed(() => {
  // 选择移动端显示的重要列
  return props.columns.filter(column => column.mobileShow !== false).slice(0, 4)
})

const paginationLayout = computed(() => {
  return isMobile.value ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'
})

// 方法
const getRowKey = (row, index) => {
  if (typeof props.rowKey === 'function') {
    return props.rowKey(row)
  }
  return row[props.rowKey] || index
}

const formatValue = (value, column) => {
  if (column.formatter && typeof column.formatter === 'function') {
    return column.formatter(row, column, value)
  }
  if (column.enum) {
    return column.enum[value] || value
  }
  return value
}

const handleRowClick = (row, index) => {
  emit('row-click', row, index)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  emit('size-change', size)
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  emit('current-change', page)
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
}

// 生命周期
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.responsive-table-container {
  width: 100%;
}

/* 移动端卡片列表 */
.mobile-card-list {
  width: 100%;
}

.mobile-card-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.mobile-card-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.mobile-card-content {
  margin-bottom: 12px;
}

.mobile-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}

.mobile-field:last-child {
  border-bottom: none;
}

.field-label {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
  min-width: 80px;
}

.field-value {
  color: #303133;
  font-size: 14px;
  text-align: right;
  flex: 1;
  word-break: break-all;
}

.mobile-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
}

/* 分页器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mobile-card-item {
    padding: 12px;
    margin-bottom: 8px;
  }

  .field-label {
    font-size: 13px;
    min-width: 70px;
  }

  .field-value {
    font-size: 13px;
  }

  .mobile-card-actions {
    flex-wrap: wrap;
    gap: 6px;
  }
}

@media (max-width: 480px) {
  .mobile-card-item {
    padding: 10px;
  }

  .mobile-field {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .field-label {
    min-width: auto;
    font-size: 12px;
  }

  .field-value {
    font-size: 12px;
    text-align: left;
  }
}
</style>
