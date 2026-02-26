#!/bin/bash

# ArkOne 测序流程配置系统 - 原型启动脚本

echo "🧬 ArkOne 测序流程配置系统 - HTML原型"
echo "=========================================="
echo ""

# 检查是否安装了Python
if command -v python3 &> /dev/null; then
    echo "✓ 检测到 Python 3"
    echo "📡 启动本地服务器..."
    echo "🌐 访问地址: http://localhost:8000"
    echo ""
    echo "按 Ctrl+C 停止服务器"
    echo ""
    cd "$(dirname "$0")"
    python3 -m http.server 8000
elif command -v python &> /dev/null; then
    echo "✓ 检测到 Python 2"
    echo "📡 启动本地服务器..."
    echo "🌐 访问地址: http://localhost:8000"
    echo ""
    echo "按 Ctrl+C 停止服务器"
    echo ""
    cd "$(dirname "$0")"
    python -m SimpleHTTPServer 8000
else
    echo "❌ 未检测到 Python"
    echo ""
    echo "请选择以下方式之一:"
    echo "1. 安装 Python: https://www.python.org/downloads/"
    echo "2. 直接用浏览器打开 index.html"
    echo "3. 使用其他本地服务器 (如 Node.js http-server)"
    echo ""

    # 尝试直接打开浏览器
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "尝试直接打开浏览器..."
        open index.html
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        xdg-open index.html
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        start index.html
    fi
fi
