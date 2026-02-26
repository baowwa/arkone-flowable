#!/bin/bash

# ArkOne Flowable API 接口测试脚本
# 测试所有核心API接口的功能和数据正确性

BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 存储测试数据
PROJECT_ID=""
CONTAINER_ID=""
SAMPLE_IDS=()

# 打印分隔线
print_separator() {
    echo -e "${BLUE}========================================${NC}"
}

# 打印测试标题
print_test() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "\n${YELLOW}[测试 $TOTAL_TESTS] $1${NC}"
}

# 打印成功
print_success() {
    PASSED_TESTS=$((PASSED_TESTS + 1))
    echo -e "${GREEN}✓ 成功: $1${NC}"
}

# 打印失败
print_fail() {
    FAILED_TESTS=$((FAILED_TESTS + 1))
    echo -e "${RED}✗ 失败: $1${NC}"
}

# 打印信息
print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# 检查HTTP状态码
check_status() {
    local status=$1
    local expected=$2
    if [ "$status" -eq "$expected" ]; then
        return 0
    else
        return 1
    fi
}

# 提取JSON字段
extract_json() {
    echo "$1" | grep -o "\"$2\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed 's/.*: *"\(.*\)".*/\1/'
}

print_separator
echo -e "${BLUE}ArkOne Flowable API 接口测试${NC}"
echo -e "${BLUE}测试时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
print_separator

# ============================================
# 1. 健康检查
# ============================================
print_test "健康检查 - 检查后端服务是否正常运行"
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null)
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    print_success "后端服务运行正常"
    print_info "响应: $body"
else
    print_fail "后端服务异常 (HTTP $http_code)"
    echo "响应: $body"
    exit 1
fi

# ============================================
# 2. 准备测试数据 - 创建项目
# ============================================
print_test "准备测试数据 - 创建测试项目"
# 注意: 这里假设有项目创建接口,如果没有则需要手动在数据库中创建
# 暂时使用固定的UUID
PROJECT_ID="11111111-1111-1111-1111-111111111111"
print_info "使用项目ID: $PROJECT_ID"

# ============================================
# 3. 准备测试数据 - 创建容器(96孔板)
# ============================================
print_test "准备测试数据 - 创建96孔板容器"
# 注意: 这里假设有容器创建接口,如果没有则需要手动在数据库中创建
# 暂时使用固定的UUID
CONTAINER_ID="22222222-2222-2222-2222-222222222222"
print_info "使用容器ID: $CONTAINER_ID"

# ============================================
# 4. 测试样本创建 - 单个样本
# ============================================
print_test "样本管理 - 创建单个样本"
payload=$(cat <<EOF
{
  "sampleName": "测试样本-001",
  "projectId": "$PROJECT_ID",
  "sampleType": "plasmid_plate",
  "containerId": "$CONTAINER_ID",
  "position": "A1",
  "description": "API测试样本"
}
EOF
)

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    sample_id=$(echo "$body" | grep -o '"id"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/.*: *"\(.*\)".*/\1/')
    sample_code=$(echo "$body" | grep -o '"sampleCode"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"\(.*\)".*/\1/')

    if [ -n "$sample_id" ]; then
        SAMPLE_IDS+=("$sample_id")
        print_success "样本创建成功"
        print_info "样本ID: $sample_id"
        print_info "样本编码: $sample_code"
    else
        print_fail "响应中未找到样本ID"
        echo "响应: $body"
    fi
else
    print_fail "样本创建失败 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 5. 测试样本查询 - 获取样本详情
# ============================================
if [ ${#SAMPLE_IDS[@]} -gt 0 ]; then
    print_test "样本管理 - 获取样本详情"
    sample_id="${SAMPLE_IDS[0]}"

    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/samples/$sample_id")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if check_status "$http_code" 200; then
        print_success "获取样本详情成功"
        print_info "响应: $body"
    else
        print_fail "获取样本详情失败 (HTTP $http_code)"
        echo "响应: $body"
    fi
fi

# ============================================
# 6. 测试样本查询 - 分页查询
# ============================================
print_test "样本管理 - 分页查询样本列表"
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/samples?current=1&size=10")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    print_success "分页查询成功"
    print_info "响应: $body"
else
    print_fail "分页查询失败 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 7. 测试样本状态更新
# ============================================
if [ ${#SAMPLE_IDS[@]} -gt 0 ]; then
    print_test "样本管理 - 更新样本状态"
    sample_id="${SAMPLE_IDS[0]}"

    response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/samples/$sample_id/status?status=in_progress")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if check_status "$http_code" 200; then
        print_success "更新样本状态成功"
        print_info "响应: $body"
    else
        print_fail "更新样本状态失败 (HTTP $http_code)"
        echo "响应: $body"
    fi
fi

# ============================================
# 8. 测试批量创建样本 - 小批量(10个)
# ============================================
print_test "批量操作 - 批量创建10个样本"
samples_json=""
for i in {1..10}; do
    row=$((($i - 1) / 12 + 1))
    col=$((($i - 1) % 12 + 1))
    row_letter=$(printf "\\$(printf '%03o' $((64 + row)))")
    position="${row_letter}${col}"

    if [ $i -eq 1 ]; then
        samples_json="{"
    else
        samples_json="${samples_json},{"
    fi

    samples_json="${samples_json}\"sampleName\":\"批量测试样本-${i}\",\"projectId\":\"$PROJECT_ID\",\"sampleType\":\"plasmid_plate\",\"containerId\":\"$CONTAINER_ID\",\"position\":\"$position\"}"
done

payload=$(cat <<EOF
{
  "samples": [$samples_json],
  "failureMode": "partial"
}
EOF
)

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/batch/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    success_count=$(echo "$body" | grep -o '"successCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')
    print_success "批量创建成功"
    print_info "成功数量: $success_count"
    print_info "响应: $body"
else
    print_fail "批量创建失败 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 9. 测试批量创建样本 - 96个样本(完整板)
# ============================================
print_test "批量操作 - 批量创建96个样本(完整96孔板)"
print_info "这是MVP的核心功能,测试96孔板批量录入效率"

# 生成96个样本的JSON
samples_json=""
for i in {1..96}; do
    row=$((($i - 1) / 12 + 1))
    col=$((($i - 1) % 12 + 1))
    row_letter=$(printf "\\$(printf '%03o' $((64 + row)))")
    position="${row_letter}${col}"

    if [ $i -eq 1 ]; then
        samples_json="{"
    else
        samples_json="${samples_json},{"
    fi

    samples_json="${samples_json}\"sampleName\":\"96孔板样本-${i}\",\"projectId\":\"$PROJECT_ID\",\"sampleType\":\"plasmid_plate\",\"position\":\"$position\"}"
done

payload=$(cat <<EOF
{
  "samples": [$samples_json],
  "failureMode": "partial"
}
EOF
)

start_time=$(date +%s%3N)
response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/batch/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
end_time=$(date +%s%3N)
elapsed=$((end_time - start_time))

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    success_count=$(echo "$body" | grep -o '"successCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')
    print_success "批量创建96个样本成功"
    print_info "成功数量: $success_count"
    print_info "响应时间: ${elapsed}ms"

    if [ "$elapsed" -lt 5000 ]; then
        print_success "性能优秀: 响应时间 < 5秒"
    elif [ "$elapsed" -lt 10000 ]; then
        print_info "性能良好: 响应时间 < 10秒"
    else
        print_fail "性能需要优化: 响应时间 > 10秒"
    fi
else
    print_fail "批量创建96个样本失败 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 10. 测试字段定义查询
# ============================================
print_test "字段定义 - 获取节点字段定义"
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/fields/nodes/shake_culture")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 200; then
    print_success "获取字段定义成功"
    print_info "响应: $body"
else
    print_fail "获取字段定义失败 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 11. 测试错误处理 - 重复样本名称
# ============================================
print_test "错误处理 - 测试重复样本名称"
payload=$(cat <<EOF
{
  "sampleName": "测试样本-001",
  "projectId": "$PROJECT_ID",
  "sampleType": "plasmid_plate",
  "containerId": "$CONTAINER_ID",
  "position": "B1"
}
EOF
)

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 400 || check_status "$http_code" 409; then
    print_success "正确处理重复样本名称错误"
    print_info "响应: $body"
else
    print_fail "未正确处理重复样本名称 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 12. 测试错误处理 - 孔位占用
# ============================================
print_test "错误处理 - 测试孔位占用冲突"
payload=$(cat <<EOF
{
  "sampleName": "测试样本-冲突",
  "projectId": "$PROJECT_ID",
  "sampleType": "plasmid_plate",
  "containerId": "$CONTAINER_ID",
  "position": "A1"
}
EOF
)

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 400 || check_status "$http_code" 409; then
    print_success "正确处理孔位占用错误"
    print_info "响应: $body"
else
    print_fail "未正确处理孔位占用 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 13. 测试错误处理 - 必填字段验证
# ============================================
print_test "错误处理 - 测试必填字段验证"
payload=$(cat <<EOF
{
  "sampleName": "",
  "projectId": "$PROJECT_ID",
  "sampleType": "plasmid_plate"
}
EOF
)

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/samples" \
    -H "$CONTENT_TYPE" \
    -d "$payload")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if check_status "$http_code" 400; then
    print_success "正确处理必填字段验证"
    print_info "响应: $body"
else
    print_fail "未正确处理必填字段验证 (HTTP $http_code)"
    echo "响应: $body"
fi

# ============================================
# 14. 测试CORS配置
# ============================================
print_test "CORS配置 - 测试跨域请求"
response=$(curl -s -w "\n%{http_code}" -X OPTIONS "$BASE_URL/samples" \
    -H "Origin: http://localhost:5173" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: Content-Type")
http_code=$(echo "$response" | tail -n1)

if check_status "$http_code" 200 || check_status "$http_code" 204; then
    print_success "CORS配置正确"
else
    print_fail "CORS配置可能有问题 (HTTP $http_code)"
fi

# ============================================
# 测试总结
# ============================================
print_separator
echo -e "${BLUE}测试总结${NC}"
print_separator
echo -e "总测试数: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}✓ 所有测试通过!${NC}"
    exit 0
else
    echo -e "\n${RED}✗ 有 $FAILED_TESTS 个测试失败${NC}"
    exit 1
fi
