#!/bin/bash

# 快速接口测试脚本 - 用于前后端联调前的快速验证

BASE_URL="http://localhost:8080/api"

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== ArkOne Flowable API 快速测试 ===${NC}\n"

# 测试计数
PASS=0
FAIL=0

# 测试函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local expected_code=$5

    echo -n "测试: $name ... "

    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        PASS=$((PASS + 1))
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code, expected $expected_code)"
        echo "  Response: $body"
        FAIL=$((FAIL + 1))
    fi
}

# 1. 健康检查
test_api "健康检查" "GET" "/actuator/health" "" "200"

# 2. 创建样本
SAMPLE_DATA='{
  "sampleName": "快速测试-'$(date +%s)'",
  "projectId": "11111111-1111-1111-1111-111111111111",
  "sampleType": "plasmid_plate",
  "position": "A1"
}'
test_api "创建样本" "POST" "/samples" "$SAMPLE_DATA" "200"

# 3. 查询样本列表
test_api "查询样本列表" "GET" "/samples?current=1&size=10" "" "200"

# 4. 批量创建样本
BATCH_DATA='{
  "samples": [
    {"sampleName": "批量-1", "projectId": "11111111-1111-1111-1111-111111111111", "sampleType": "plasmid_plate", "position": "B1"},
    {"sampleName": "批量-2", "projectId": "11111111-1111-1111-1111-111111111111", "sampleType": "plasmid_plate", "position": "B2"}
  ],
  "failureMode": "partial"
}'
test_api "批量创建样本" "POST" "/batch/samples" "$BATCH_DATA" "200"

# 5. 获取字段定义
test_api "获取字段定义" "GET" "/fields/nodes/shake_culture" "" "200"

# 总结
echo -e "\n${YELLOW}=== 测试总结 ===${NC}"
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"

if [ $FAIL -eq 0 ]; then
    echo -e "\n${GREEN}✓ 所有测试通过! 可以开始前后端联调${NC}"
    exit 0
else
    echo -e "\n${RED}✗ 有 $FAIL 个测试失败,请检查后端服务${NC}"
    exit 1
fi
