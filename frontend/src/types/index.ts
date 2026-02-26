// 样本状态
export type SampleStatus = 'pending' | 'in_progress' | 'paused' | 'exception' | 'completed' | 'cancelled'

// 样本类型
export type SampleType =
  | 'plasmid_plate'
  | 'plasmid_pellet'
  | 'plasmid_liquid'
  | 'plasmid_nucleic_acid'
  | 'pcr_product_original'
  | 'pcr_product_purified'

// 字段类型
export type FieldType =
  | 'text'
  | 'number'
  | 'date'
  | 'datetime'
  | 'select'
  | 'textarea'
  | 'formula'

// 样本接口
export interface Sample {
  id: string
  sampleCode: string
  sampleName: string
  projectId: string
  sampleType: SampleType
  status: SampleStatus
  currentNode?: string
  containerId?: string
  position?: string
  parentSampleId?: string
  createdBy?: string
  createdAt: string
  updatedAt: string
  deleted: boolean
}

// 项目接口
export interface Project {
  id: string
  projectCode: string
  projectName: string
  description?: string
  processDefinitionId?: string
  status: string
  createdBy?: string
  createdAt: string
  updatedAt: string
}

// 容器接口
export interface Container {
  id: string
  containerCode: string
  containerType: string
  capacity: number
  usedCount: number
  status: string
  createdBy?: string
  createdAt: string
  updatedAt: string
}

// 字段定义接口
export interface FieldDefinition {
  id: string
  nodeId: string
  fieldName: string
  fieldLabel: string
  fieldType: FieldType
  unit?: string
  required: boolean
  defaultValue?: string
  validationRule?: ValidationRule
  formula?: string
  options?: FieldOption[]
  displayOrder: number
  createdAt: string
  updatedAt: string
}

// 验证规则接口
export interface ValidationRule {
  min?: number
  max?: number
  pattern?: string
  message?: string
}

// 字段选项接口
export interface FieldOption {
  label: string
  value: string
}

// 节点数据接口
export interface NodeData {
  id: string
  sampleId: string
  nodeId: string
  nodeName: string
  data: Record<string, any>
  status: string
  createdBy?: string
  createdAt: string
  updatedAt: string
}

// 任务接口
export interface Task {
  id: string
  name: string
  processInstanceId: string
  processDefinitionId: string
  taskDefinitionKey: string
  assignee?: string
  createTime: string
  dueDate?: string
  priority: number
  suspended: boolean
}

// 批量操作结果
export interface BatchResult<T = any> {
  success: boolean
  data?: T
  error?: string
  index?: number
}

// 批量操作响应(后端格式)
export interface BatchOperationResult<T> {
  successCount: number
  failureCount: number
  results: Array<{
    success: boolean
    data?: T
    error?: string
  }>
}

// 分页参数
export interface PageParams {
  pageNum: number
  pageSize: number
  sort?: string
  order?: 'asc' | 'desc'
}

// 分页结果 (MyBatis-Plus Page 格式)
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages?: number
}

// API响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 批量样本数据
export interface BatchSampleData {
  index: number
  sampleId: string
  position: string
  data: Record<string, any>
  errors: Record<string, string>
  completed: boolean
}

// 板位图孔位
export interface WellPosition {
  position: string
  row: number
  col: number
  status: 'empty' | 'used' | 'exception'
  sampleId?: string
  sampleName?: string
  currentNode?: string
}

// 表单字段值
export interface FormFieldValue {
  fieldName: string
  value: any
  error?: string
}
