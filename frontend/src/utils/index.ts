import { evaluate } from 'mathjs'

/**
 * 格式化日期
 */
export function formatDate(date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  const d = typeof date === 'string' ? new Date(date) : date

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  let previous = 0

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    const remaining = wait - (now - previous)

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(this, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(this, args)
      }, remaining)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as any
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as any
  if (obj instanceof Object) {
    const clonedObj = {} as T
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  return obj
}

/**
 * 生成UUID
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 下载文件
 */
export function downloadFile(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 计算公式
 * 使用mathjs进行安全计算
 */
export function calculateFormula(
  formula: string,
  variables: Record<string, number>
): number | null {
  try {
    // 白名单函数
    const allowedFunctions = ['max', 'min', 'round', 'abs', 'ceil', 'floor']

    // 检查公式是否只包含允许的函数
    const functionPattern = /\b([a-zA-Z_]\w*)\s*\(/g
    const matches = formula.matchAll(functionPattern)

    for (const match of matches) {
      const funcName = match[1]
      if (!allowedFunctions.includes(funcName)) {
        console.error(`不允许的函数: ${funcName}`)
        return null
      }
    }

    // 限制公式长度
    if (formula.length > 500) {
      console.error('公式长度超过限制')
      return null
    }

    // 计算公式
    const result = evaluate(formula, variables)

    return typeof result === 'number' ? result : null
  } catch (error) {
    console.error('公式计算错误:', error)
    return null
  }
}

/**
 * 验证字段值
 */
export function validateFieldValue(
  value: any,
  fieldType: string,
  validationRule?: {
    min?: number
    max?: number
    pattern?: string
    message?: string
  }
): { valid: boolean; message?: string } {
  // 空值检查
  if (value === null || value === undefined || value === '') {
    return { valid: true }
  }

  // 数字类型验证
  if (fieldType === 'number') {
    const num = Number(value)
    if (isNaN(num)) {
      return { valid: false, message: '请输入有效的数字' }
    }
    if (validationRule?.min !== undefined && num < validationRule.min) {
      return { valid: false, message: `值不能小于 ${validationRule.min}` }
    }
    if (validationRule?.max !== undefined && num > validationRule.max) {
      return { valid: false, message: `值不能大于 ${validationRule.max}` }
    }
  }

  // 正则表达式验证
  if (validationRule?.pattern) {
    const regex = new RegExp(validationRule.pattern)
    if (!regex.test(String(value))) {
      return {
        valid: false,
        message: validationRule.message || '格式不正确'
      }
    }
  }

  return { valid: true }
}

/**
 * 生成板位编号
 * @param row 行索引 (0-7)
 * @param col 列索引 (0-11)
 * @returns 板位编号 (如: A01, B12)
 */
export function generatePosition(row: number, col: number): string {
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  return `${rows[row]}${String(col + 1).padStart(2, '0')}`
}

/**
 * 解析板位编号
 * @param position 板位编号 (如: A01)
 * @returns { row: number, col: number }
 */
export function parsePosition(position: string): { row: number; col: number } | null {
  const match = position.match(/^([A-H])(\d{2})$/)
  if (!match) return null

  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  const row = rows.indexOf(match[1])
  const col = parseInt(match[2]) - 1

  if (row === -1 || col < 0 || col > 11) return null

  return { row, col }
}

/**
 * 本地存储工具
 */
export const storage = {
  get<T>(key: string): T | null {
    try {
      const value = localStorage.getItem(key)
      return value ? JSON.parse(value) : null
    } catch {
      return null
    }
  },

  set(key: string, value: any): void {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('存储失败:', error)
    }
  },

  remove(key: string): void {
    localStorage.removeItem(key)
  },

  clear(): void {
    localStorage.clear()
  }
}
