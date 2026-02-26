<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { calculateFormula, validateFieldValue } from '@/utils'
import type { FieldDefinition, FormFieldValue } from '@/types'

interface Props {
  fields: FieldDefinition[]
  modelValue: Record<string, any>
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, any>): void
  (e: 'validate', valid: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
})

const emit = defineEmits<Emits>()

// 表单数据
const formData = ref<Record<string, any>>({ ...props.modelValue })
const errors = ref<Record<string, string>>({})

// 监听外部数据变化
watch(
  () => props.modelValue,
  (newValue) => {
    formData.value = { ...newValue }
  },
  { deep: true }
)

// 监听表单数据变化
watch(
  formData,
  (newValue) => {
    emit('update:modelValue', newValue)
  },
  { deep: true }
)

// 处理字段输入
const handleFieldInput = (field: FieldDefinition, value: any) => {
  formData.value[field.fieldName] = value

  // 如果是公式字段的依赖字段,重新计算公式
  props.fields.forEach(f => {
    if (f.formula && f.formula.includes(field.fieldName)) {
      const result = calculateFormula(f.formula, formData.value)
      if (result !== null) {
        formData.value[f.fieldName] = result
      }
    }
  })

  // 验证字段
  validateField(field)

  // 触发验证事件
  const isValid = Object.keys(errors.value).length === 0
  emit('validate', isValid)
}

// 验证字段
const validateField = (field: FieldDefinition) => {
  const value = formData.value[field.fieldName]

  // 必填验证
  if (field.required && (value === null || value === undefined || value === '')) {
    errors.value[field.fieldName] = '此字段为必填项'
    return
  }

  // 类型验证
  const validation = validateFieldValue(value, field.fieldType, field.validationRule)
  if (!validation.valid) {
    errors.value[field.fieldName] = validation.message || '验证失败'
  } else {
    delete errors.value[field.fieldName]
  }
}

// 验证所有字段
const validateAll = (): boolean => {
  errors.value = {}

  props.fields.forEach(field => {
    validateField(field)
  })

  const isValid = Object.keys(errors.value).length === 0
  emit('validate', isValid)
  return isValid
}

// 重置表单
const reset = () => {
  formData.value = {}
  errors.value = {}
}

// 暴露方法
defineExpose({
  validateAll,
  reset
})
</script>

<template>
  <el-form :model="formData" label-width="120px" :disabled="disabled">
    <el-form-item
      v-for="field in fields"
      :key="field.id"
      :label="field.fieldLabel"
      :required="field.required"
      :error="errors[field.fieldName]"
    >
      <!-- 文本输入 -->
      <el-input
        v-if="field.fieldType === 'text'"
        v-model="formData[field.fieldName]"
        :placeholder="field.defaultValue || `请输入${field.fieldLabel}`"
        @input="handleFieldInput(field, formData[field.fieldName])"
        @blur="validateField(field)"
      >
        <template v-if="field.unit" #append>{{ field.unit }}</template>
      </el-input>

      <!-- 数字输入 -->
      <el-input-number
        v-else-if="field.fieldType === 'number'"
        v-model="formData[field.fieldName]"
        :placeholder="field.defaultValue || `请输入${field.fieldLabel}`"
        :min="field.validationRule?.min"
        :max="field.validationRule?.max"
        :disabled="!!field.formula"
        :controls-position="'right'"
        style="width: 100%"
        @change="handleFieldInput(field, formData[field.fieldName])"
        @blur="validateField(field)"
      />

      <!-- 日期选择 -->
      <el-date-picker
        v-else-if="field.fieldType === 'date'"
        v-model="formData[field.fieldName]"
        type="date"
        placeholder="请选择日期"
        style="width: 100%"
        @change="handleFieldInput(field, formData[field.fieldName])"
      />

      <!-- 日期时间选择 -->
      <el-date-picker
        v-else-if="field.fieldType === 'datetime'"
        v-model="formData[field.fieldName]"
        type="datetime"
        placeholder="请选择日期时间"
        style="width: 100%"
        @change="handleFieldInput(field, formData[field.fieldName])"
      />

      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.fieldType === 'select'"
        v-model="formData[field.fieldName]"
        placeholder="请选择"
        style="width: 100%"
        @change="handleFieldInput(field, formData[field.fieldName])"
      >
        <el-option
          v-for="option in field.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>

      <!-- 多行文本 -->
      <el-input
        v-else-if="field.fieldType === 'textarea'"
        v-model="formData[field.fieldName]"
        type="textarea"
        :rows="3"
        :placeholder="field.defaultValue || `请输入${field.fieldLabel}`"
        @input="handleFieldInput(field, formData[field.fieldName])"
        @blur="validateField(field)"
      />

      <!-- 公式字段(只读) -->
      <el-input
        v-else-if="field.fieldType === 'formula'"
        v-model="formData[field.fieldName]"
        disabled
        :placeholder="'自动计算'"
      >
        <template v-if="field.unit" #append>{{ field.unit }}</template>
      </el-input>

      <!-- 默认文本输入 -->
      <el-input
        v-else
        v-model="formData[field.fieldName]"
        :placeholder="field.defaultValue || `请输入${field.fieldLabel}`"
        @input="handleFieldInput(field, formData[field.fieldName])"
        @blur="validateField(field)"
      />

      <!-- 字段说明 -->
      <template v-if="field.formula" #extra>
        <span class="field-hint">公式: {{ field.formula }}</span>
      </template>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.field-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
