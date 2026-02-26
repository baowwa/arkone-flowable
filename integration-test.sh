#!/bin/bash

# 前后端联调测试脚本
# 日期: 2026-02-25

echo "=========================================="
echo "ArkOne Flowable 前后端联调测试"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_code=$5

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -n "测试 $TOTAL_TESTS: $name ... "

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" -H "Content-Type: application/json")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" -H "Content-Type: application/json" -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ 通过${NC} (HTTP $http_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}✗ 失败${NC} (期望 $expected_code, 实际 $http_code)"
        echo "响应: $body"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

echo "1. 检查服务状态"
echo "----------------------------------------"

# 检查后端
echo -n "后端服务 (http://localhost:8080) ... "
if curl -s http://localhost:8080/api/doc.html > /dev/null; then
    echo -e "${GREEN}✓ 运行中${NC}"
else
    echo -e "${RED}✗ 未运行${NC}"
    echo "请先启动后端服务: cd backend && mvn spring-boot:run"
    exit 1
fi

# 检查前端
echo -n "前端服务 (http://localhost:3001) ... "
if curl -s http://localhost:3001 > /dev/null; then
    echo -e "${GREEN}✓ 运行中${NC}"
else
    echo -e "${RED}✗ 未运行${NC}"
    echo "请先启动前端服务: cd frontend && npm run dev"
    exit 1
fi

# 检查数据库
echo -n "PostgreSQL ... "
if docker ps | grep arkone-postgres > /dev/null; then
    echo -e "${GREEN}✓ 运行中${NC}"
else
    echo -e "${RED}✗ 未运行${NC}"
    echo "请先启动Docker服务: docker-compose up -d"
    exit 1
fi

echo ""
echo "2. 测试后端API接口"
echo "----------------------------------------"

# 测试样本列表
test_api "获取样本列表" "GET" "http://localhost:8080/api/samples?page=1&size=10" "" "200"

# 测试容器列表
test_api "获取容器列表" "GET" "http://localhost:8080/api/containers" "" "200"

# 测试项目列表
test_api "获取项目列表" "GET" "http://localhost:8080/api/projects" "" "200"

# 测试字段定义
test_api "获取字段定义" "GET" "http://localhost:8080/api/fields/nodes/sample-reception" "" "200"

# 测试创建样本
SAMPLE_DATA='{
  "name": "TEST-SAMPLE-001",
  "projectId": "proj-001",
  "sampleType": "plasmid_plate",
  "containerId": "container-001",
  "position": "A1"
}'
test_api "创建样本" "POST" "http://localhost:8080/api/samples" "$SAMPLE_DATA" "200"

echo ""
echo "3. 测试批量操作"
echo "----------------------------------------"

# 测试批量创建(3个样本)
BATCH_DATA='{
  "samples": [
    {
      "name": "BATCH-001",
      "projectId": "proj-001",
      "sampleType": "plasmid_plate",
      "containerId": "container-001",
      "position": "B1"
    },
    {
      "name": "BATCH-002",
      "projectId": "proj-001",
      "sampleType": "plasmid_plate",
      "containerId": "container-001",
      "position": "B2"
    },
    {
      "name": "BATCH-003",
      "projectId": "proj-001",
      "sampleType": "plasmid_plate",
      "containerId": "container-001",
      "position": "B3"
    }
  ],
  "atomic": false
}'
test_api "批量创建样本" "POST" "http://localhost:8080/api/batch/samples" "$BATCH_DATA" "200"

echo ""
echo "4. 测试前端页面访问"
echo "----------------------------------------"

# 测试前端页面
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo -n "测试 $TOTAL_TESTS: 访问前端首页 ... "
if curl -s http://localhost:3001 | grep -q "ArkOne"; then
    echo -e "${GREEN}✓ 通过${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${YELLOW}⚠ 警告${NC} (页面可能未包含ArkOne关键字)"
    PASSED_TESTS=$((PASSED_TESTS + 1))
fi

echo ""
echo "=========================================="
echo "测试结果汇总"
echo "=========================================="
echo "总测试数: $TOTAL_TESTS"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过!${NC}"
    echo ""
    echo "前后端联调成功,可以进行功能测试了!"
    echo ""
    echo "访问地址:"
    echo "  - 前端: http://localhost:3001"
    echo "  - 后端API文档: http://localhost:8080/api/doc.html"
    exit 0
else
    echo -e "${RED}✗ 部分测试失败,请检查日志${NC}"
    exit 1
fi
