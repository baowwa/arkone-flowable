// ArkOne 测序流程配置系统 - 公共脚本

// 模拟数据
const mockData = {
  samples: [
    {
      id: 'AK20260224001',
      name: 'Sample-01',
      project: '全质粒测序',
      type: '平板样本',
      currentNode: '核酸提取',
      status: 'in_progress',
      submitter: '张三',
      submitTime: '2026-02-24 09:30'
    },
    {
      id: 'AK20260224002',
      name: 'Sample-02',
      project: 'PCR产物测序',
      type: 'PCR原液',
      currentNode: '文库构建',
      status: 'in_progress',
      submitter: '李四',
      submitTime: '2026-02-24 10:15'
    },
    {
      id: 'AK20260223156',
      name: 'Sample-56',
      project: '全质粒测序',
      type: '质粒核酸',
      currentNode: '已完成',
      status: 'completed',
      submitter: '王五',
      submitTime: '2026-02-23 14:20'
    },
    {
      id: 'AK20260223155',
      name: 'Sample-55',
      project: 'PCR产物测序',
      type: '已纯化',
      currentNode: '样本前处理',
      status: 'in_progress',
      submitter: '赵六',
      submitTime: '2026-02-23 11:00'
    }
  ],

  processSteps: {
    '全质粒测序': ['摇菌', '核酸提取', '样本前处理', '文库构建', '上机测序'],
    'PCR产物测序': ['样本前处理', '文库构建', '测序复合物制备及纯化', '上机测序']
  },

  sampleTypes: {
    '全质粒测序': ['平板样本', '沉菌', '菌液样本', '直抽菌液', '质粒核酸样本'],
    'PCR产物测序': ['PCR原液', '已纯化']
  },

  containers: {
    '96孔板': { rows: 8, cols: 12, positions: [] },
    '48孔板': { rows: 6, cols: 8, positions: [] }
  }
};

// 初始化96孔板位置
function initPlatePositions(type) {
  const config = mockData.containers[type];
  const positions = [];
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

  for (let r = 0; r < config.rows; r++) {
    for (let c = 0; c < config.cols; c++) {
      const position = `${rows[r]}${String(c + 1).padStart(2, '0')}`;
      const isUsed = Math.random() > 0.5; // 随机生成使用状态
      positions.push({
        position,
        status: isUsed ? 'used' : 'empty',
        sampleId: isUsed ? `AK2026022400${Math.floor(Math.random() * 100)}` : null
      });
    }
  }

  return positions;
}

// 格式化日期时间
function formatDateTime(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 生成样本编号
function generateSampleId() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0');
  return `AK${year}${month}${day}${random}`;
}

// 显示提示消息
function showMessage(message, type = 'success') {
  const messageBox = document.createElement('div');
  messageBox.className = `message-box message-${type}`;
  messageBox.textContent = message;
  messageBox.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    background: ${type === 'success' ? '#67C23A' : type === 'error' ? '#F56C6C' : '#409EFF'};
    color: white;
    border-radius: 4px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
  `;

  document.body.appendChild(messageBox);

  setTimeout(() => {
    messageBox.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => {
      document.body.removeChild(messageBox);
    }, 300);
  }, 3000);
}

// 添加动画样式
const style = document.createElement('style');
style.textContent = `
  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }

  @keyframes slideOut {
    from {
      transform: translateX(0);
      opacity: 1;
    }
    to {
      transform: translateX(100%);
      opacity: 0;
    }
  }
`;
document.head.appendChild(style);

// 表单验证
function validateForm(formId) {
  const form = document.getElementById(formId);
  if (!form) return false;

  const requiredFields = form.querySelectorAll('[required]');
  let isValid = true;

  requiredFields.forEach(field => {
    const errorElement = field.parentElement.querySelector('.form-error');

    if (!field.value.trim()) {
      isValid = false;
      field.style.borderColor = 'var(--danger-color)';

      if (errorElement) {
        errorElement.textContent = '此字段为必填项';
        errorElement.style.display = 'block';
      }
    } else {
      field.style.borderColor = 'var(--border-color)';

      if (errorElement) {
        errorElement.style.display = 'none';
      }
    }
  });

  return isValid;
}

// 数字输入验证
function validateNumber(input, min, max) {
  const value = parseFloat(input.value);
  const errorElement = input.parentElement.querySelector('.form-error');

  if (isNaN(value)) {
    input.style.borderColor = 'var(--danger-color)';
    if (errorElement) {
      errorElement.textContent = '请输入有效的数字';
      errorElement.style.display = 'block';
    }
    return false;
  }

  if (min !== undefined && value < min) {
    input.style.borderColor = 'var(--danger-color)';
    if (errorElement) {
      errorElement.textContent = `数值不能小于 ${min}`;
      errorElement.style.display = 'block';
    }
    return false;
  }

  if (max !== undefined && value > max) {
    input.style.borderColor = 'var(--danger-color)';
    if (errorElement) {
      errorElement.textContent = `数值不能大于 ${max}`;
      errorElement.style.display = 'block';
    }
    return false;
  }

  input.style.borderColor = 'var(--success-color)';
  if (errorElement) {
    errorElement.style.display = 'none';
  }
  return true;
}

// 公式计算
function calculateFormula(formula, values) {
  try {
    let expression = formula;

    // 替换变量
    Object.keys(values).forEach(key => {
      const value = values[key];
      if (typeof value === 'number') {
        expression = expression.replace(new RegExp(key, 'g'), value);
      }
    });

    // 简单的数学表达式计算
    const result = eval(expression);
    return typeof result === 'number' ? result.toFixed(2) : 0;
  } catch (error) {
    console.error('公式计算错误:', error);
    return 0;
  }
}

// 扫码枪输入处理
let barcodeBuffer = '';
let lastKeyTime = 0;

document.addEventListener('keypress', (event) => {
  const currentTime = Date.now();

  // 扫码枪输入间隔通常 < 50ms
  if (currentTime - lastKeyTime > 50) {
    barcodeBuffer = '';
  }

  lastKeyTime = currentTime;

  if (event.key === 'Enter') {
    if (barcodeBuffer.length > 0) {
      handleBarcodeScanned(barcodeBuffer);
      barcodeBuffer = '';
    }
  } else {
    barcodeBuffer += event.key;
  }
});

function handleBarcodeScanned(barcode) {
  console.log('扫描到条码:', barcode);
  showMessage(`扫描到条码: ${barcode}`, 'info');

  // 查找对应的输入框并填充
  const activeElement = document.activeElement;
  if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'SELECT')) {
    activeElement.value = barcode;
    activeElement.dispatchEvent(new Event('input', { bubbles: true }));
  }
}

// 导出功能
function exportToExcel(data, filename) {
  console.log('导出数据:', data);
  showMessage(`正在导出 ${filename}...`, 'info');

  // 实际项目中这里会调用导出库
  setTimeout(() => {
    showMessage('导出成功!', 'success');
  }, 1000);
}

// 本地存储辅助函数
const storage = {
  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('存储失败:', error);
    }
  },

  get(key, defaultValue = null) {
    try {
      const value = localStorage.getItem(key);
      return value ? JSON.parse(value) : defaultValue;
    } catch (error) {
      console.error('读取失败:', error);
      return defaultValue;
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('删除失败:', error);
    }
  }
};
