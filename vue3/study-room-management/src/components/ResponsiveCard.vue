<template>
  <el-card 
    :class="['responsive-card', cardClass]"
    :shadow="shadow"
    :body-style="{ padding: bodyPadding }"
  >
    <template #header v-if="title || $slots.header">
      <div class="card-header">
        <div class="header-left">
          <slot name="icon">
            <el-icon v-if="icon" :size="18" :color="iconColor">
              <component :is="icon" />
            </el-icon>
          </slot>
          <h3 v-if="title" class="card-title">{{ title }}</h3>
        </div>
        <div class="header-right">
          <slot name="extra" />
        </div>
      </div>
    </template>

    <div class="card-body">
      <slot />
    </div>

    <template #footer v-if="$slots.footer">
      <div class="card-footer">
        <slot name="footer" />
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: String,
  icon: String,
  iconColor: {
    type: String,
    default: '#409EFF'
  },
  shadow: {
    type: String,
    default: 'hover'
  },
  padding: {
    type: [String, Number],
    default: '20px'
  },
  hoverable: {
    type: Boolean,
    default: true
  },
  bordered: {
    type: Boolean,
    default: true
  }
})

const cardClass = computed(() => {
  return {
    'hoverable': props.hoverable,
    'bordered': props.bordered
  }
})

const bodyPadding = computed(() => {
  if (typeof props.padding === 'number') {
    return `${props.padding}px`
  }
  return props.padding
})
</script>

<style scoped>
.responsive-card {
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.responsive-card.hoverable:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.responsive-card.bordered {
  border: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  min-height: 60px;
}

.card-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .responsive-card {
    margin-bottom: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-left {
    width: 100%;
  }

  .header-right {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }

  .card-title {
    font-size: 14px;
  }

  .card-body {
    padding: 16px !important;
  }
}

@media (max-width: 480px) {
  .responsive-card {
    margin-bottom: 12px;
  }

  .card-body {
    padding: 12px !important;
  }

  .card-title {
    font-size: 13px;
  }
}
</style>
