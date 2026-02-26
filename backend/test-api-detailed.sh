#!/bin/bash

# ArkOne Flowable API 详细测试脚本
# 包含数据库验证、性能测试、并发测试

BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

# 数据库连接信息
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="arkone_flowable"
DB_USER="root"
DB_PASS="123456"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 测试结果
declare -A TEST_RESULTS
TEST_COUNT=0

# 打印函数
print_header() {
    echo -e "\n${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║  $1${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
}

print_test() {
    TEST_COUNT=$((TEST_COUNT + 1))
    echo -e "\n${YELLOW}[测试 $TEST_COUNT] $1${NC}"
}

print_success() {
    echo -e "${GREEN}  ✓ $1${NC}"
}

print_fail() {
    echo -e "${RED}  ✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}  ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}  ⚠ $1${NC}"
}

# 记录测试结果
record_result() {
    local test_name=$1
    local result=$2
    TEST_RESULTS["$test_name"]=$result
}

# 执行SQL查询
query_db() {
    PGPASSWORD=$DB_PASS psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "$1" 2>/dev/null
}

# 检查数据库连接
check_db_connection() {
    print_test "检查数据库连接"
    result=$(query_db "SELECT 1;")
    if [ $? -eq 0 ]; then
        print_success "数据库连接正常"
        record_result "db_connection" "PASS"
        return 0
    else
        print_fail "数据库连接失败"
        record_result "db_connection" "FAIL"
        return 1
    fi
}

# 清理测试数据
cleanup_test_data() {
    print_info "清理测试数据..."
    query_db "DELETE FROM lims_sample WHERE sample_name LIKE '测试样本%' OR sample_name LIKE '批量测试样本%' OR sample_name LIKE '96孔板样本%';" > /dev/null 2>&1
    query_db "DELETE FROM lims_sample WHERE sample_name LIKE '性能测试%' OR sample_name LIKE '并发测试%';" > /dev/null 2>&1
}

# 准备测试环境
prepare_test_env() {
    print_header "准备测试环境"

    # 检查数据库连接
    if ! check_db_connection; then
        print_fail "无法连接数据库,跳过数据库验证测试"
        DB_AVAILABLE=false
    else
        DB_AVAILABLE=true
        cleanup_test_data
    fi

    # 检查后端服务
    print_test "检查后端服务"
    response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
    if [ "$response" = "200" ]; then
        print_success "后端服务运行正常"
        record_result "backend_health" "PASS"
    else
        print_fail "后端服务异常 (HTTP $response)"
        record_result "backend_health" "FAIL"
        exit 1
    fi

    # 准备测试数据
    print_test "准备测试数据"
    PROJECT_ID="11111111-1111-1111-1111-111111111111"
    CONTAINER_ID="22222222-2222-2222-2222-222222222222"
    print_info "项目ID: $PROJECT_ID"
    print_info "容器ID: $CONTAINER_ID"
}

# 测试1: 单个样本创建
test_single_sample_creation() {
    print_header "测试1: 单个样本创建"

    print_test "创建单个样本"
    payload=$(cat <<EOF
{
  "sampleName": "测试样本-单个-001",
  "projectId": "$PROJECT_ID",
  "sampleType": "plasmid_plate",
  "containerId": "$CONTAINER_ID",
  "position": "A1",
  "description": "单个样本创建测试"
}
EOF
)

    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/samples" \
        -H "$CONTENT_TYPE" \
        -d "$payload")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        sample_id=$(echo "$body" | grep -o '"id"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/.*: *"\(.*\)".*/\1/')
        sample_code=$(echo "$body" | grep -o '"sampleCode"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"\(.*\)".*/\1/')

        print_success "样本创建成功"
        print_info "样本ID: $sample_id"
        print_info "样本编码: $sample_code"
        record_result "single_sample_creation" "PASS"

        # 验证数据库
        if [ "$DB_AVAILABLE" = true ]; then
            print_test "验证数据库中的样本数据"
            db_sample=$(query_db "SELECT sample_name, sample_type, status FROM lims_sample WHERE id = '$sample_id';")
            if [ -n "$db_sample" ]; then
                print_success "数据库验证通过"
                print_info "数据库记录: $db_sample"
                record_result "db_verification_single" "PASS"
            else
                print_fail "数据库中未找到样本"
                record_result "db_verification_single" "FAIL"
            fi
        fi

        # 保存样本ID供后续测试使用
        SAMPLE_ID_1=$sample_id
    else
        print_fail "样本创建失败 (HTTP $http_code)"
        echo "$body"
        record_result "single_sample_creation" "FAIL"
    fi
}

# 测试2: 样本查询
test_sample_query() {
    print_header "测试2: 样本查询"

    if [ -z "$SAMPLE_ID_1" ]; then
        print_warning "跳过测试: 没有可用的样本ID"
        return
    fi

    # 测试详情查询
    print_test "查询样本详情"
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/samples/$SAMPLE_ID_1")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        print_success "查询样本详情成功"
        record_result "sample_detail_query" "PASS"
    else
        print_fail "查询样本详情失败 (HTTP $http_code)"
        record_result "sample_detail_query" "FAIL"
    fi

    # 测试列表查询
    print_test "分页查询样本列表"
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/samples?current=1&size=10")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        total=$(echo "$body" | grep -o '"total"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')
        print_success "分页查询成功"
        print_info "总记录数: $total"
        record_result "sample_list_query" "PASS"
    else
        print_fail "分页查询失败 (HTTP $http_code)"
        record_result "sample_list_query" "FAIL"
    fi

    # 测试筛选查询
    print_test "筛选查询 - 按样本类型"
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/samples?current=1&size=10&sampleType=plasmid_plate")
    http_code=$(echo "$response" | tail -n1)

    if [ "$http_code" = "200" ]; then
        print_success "筛选查询成功"
        record_result "sample_filter_query" "PASS"
    else
        print_fail "筛选查询失败 (HTTP $http_code)"
        record_result "sample_filter_query" "FAIL"
    fi
}

# 测试3: 样本状态更新
test_sample_status_update() {
    print_header "测试3: 样本状态更新"

    if [ -z "$SAMPLE_ID_1" ]; then
        print_warning "跳过测试: 没有可用的样本ID"
        return
    fi

    print_test "更新样本状态: pending -> in_progress"
    response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/samples/$SAMPLE_ID_1/status?status=in_progress")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        new_status=$(echo "$body" | grep -o '"status"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"\(.*\)".*/\1/')
        print_success "状态更新成功"
        print_info "新状态: $new_status"
        record_result "status_update" "PASS"

        # 验证数据库
        if [ "$DB_AVAILABLE" = true ]; then
            print_test "验证数据库中的状态"
            db_status=$(query_db "SELECT status FROM lims_sample WHERE id = '$SAMPLE_ID_1';" | tr -d ' ')
            if [ "$db_status" = "in_progress" ]; then
                print_success "数据库状态验证通过"
                record_result "db_status_verification" "PASS"
            else
                print_fail "数据库状态不匹配: $db_status"
                record_result "db_status_verification" "FAIL"
            fi
        fi
    else
        print_fail "状态更新失败 (HTTP $http_code)"
        echo "$body"
        record_result "status_update" "FAIL"
    fi
}

# 测试4: 批量创建样本(小批量)
test_batch_creation_small() {
    print_header "测试4: 批量创建样本(10个)"

    print_test "批量创建10个样本"

    # 生成10个样本
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

        samples_json="${samples_json}\"sampleName\":\"批量测试样本-${i}\",\"projectId\":\"$PROJECT_ID\",\"sampleType\":\"plasmid_plate\",\"position\":\"$position\"}"
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

    if [ "$http_code" = "200" ]; then
        success_count=$(echo "$body" | grep -o '"successCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')
        failure_count=$(echo "$body" | grep -o '"failureCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')

        print_success "批量创建完成"
        print_info "成功: $success_count, 失败: $failure_count"
        print_info "响应时间: ${elapsed}ms"

        if [ "$success_count" = "10" ]; then
            record_result "batch_creation_small" "PASS"
        else
            record_result "batch_creation_small" "PARTIAL"
        fi

        # 验证数据库
        if [ "$DB_AVAILABLE" = true ]; then
            print_test "验证数据库中的批量数据"
            db_count=$(query_db "SELECT COUNT(*) FROM lims_sample WHERE sample_name LIKE '批量测试样本-%';" | tr -d ' ')
            print_info "数据库记录数: $db_count"
            if [ "$db_count" = "$success_count" ]; then
                print_success "数据库验证通过"
                record_result "db_batch_verification" "PASS"
            else
                print_fail "数据库记录数不匹配"
                record_result "db_batch_verification" "FAIL"
            fi
        fi
    else
        print_fail "批量创建失败 (HTTP $http_code)"
        echo "$body"
        record_result "batch_creation_small" "FAIL"
    fi
}

# 测试5: 批量创建96个样本(MVP核心功能)
test_batch_creation_96() {
    print_header "测试5: 批量创建96个样本(MVP核心功能)"

    print_test "批量创建96个样本(完整96孔板)"
    print_info "这是MVP的核心功能,测试96孔板批量录入"

    # 生成96个样本
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

    if [ "$http_code" = "200" ]; then
        success_count=$(echo "$body" | grep -o '"successCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')
        failure_count=$(echo "$body" | grep -o '"failureCount"[[:space:]]*:[[:space:]]*[0-9]*' | sed 's/.*: *\([0-9]*\).*/\1/')

        print_success "批量创建96个样本完成"
        print_info "成功: $success_count, 失败: $failure_count"
        print_info "响应时间: ${elapsed}ms (${elapsed}秒)"

        # 性能评估
        if [ "$elapsed" -lt 5000 ]; then
            print_success "性能优秀: 响应时间 < 5秒"
            record_result "batch_96_performance" "EXCELLENT"
        elif [ "$elapsed" -lt 10000 ]; then
            print_success "性能良好: 响应时间 < 10秒"
            record_result "batch_96_performance" "GOOD"
        elif [ "$elapsed" -lt 30000 ]; then
            print_warning "性能一般: 响应时间 < 30秒"
            record_result "batch_96_performance" "FAIR"
        else
            print_fail "性能需要优化: 响应时间 > 30秒"
            record_result "batch_96_performance" "POOR"
        fi

        if [ "$success_count" = "96" ]; then
            record_result "batch_creation_96" "PASS"
        else
            record_result "batch_creation_96" "PARTIAL"
        fi
    else
        print_fail "批量创建96个样本失败 (HTTP $http_code)"
        echo "$body"
        record_result "batch_creation_96" "FAIL"
    fi
}

# 测试6: 字段定义查询
test_field_definition() {
    print_header "测试6: 字段定义查询"

    print_test "获取节点字段定义"
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/fields/nodes/shake_culture")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        field_count=$(echo "$body" | grep -o '"fieldName"' | wc -l)
        print_success "获取字段定义成功"
        print_info "字段数量: $field_count"
        record_result "field_definition_query" "PASS"
    else
        print_fail "获取字段定义失败 (HTTP $http_code)"
        record_result "field_definition_query" "FAIL"
    fi
}

# 测试7: 错误处理
test_error_handling() {
    print_header "测试7: 错误处理"

    # 测试重复样本名称
    print_test "错误处理 - 重复样本名称"
    payload=$(cat <<EOF
{
  "sampleName": "测试样本-单个-001",
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

    if [ "$http_code" = "400" ] || [ "$http_code" = "409" ]; then
        print_success "正确处理重复样本名称"
        record_result "error_duplicate_name" "PASS"
    else
        print_fail "未正确处理重复样本名称 (HTTP $http_code)"
        record_result "error_duplicate_name" "FAIL"
    fi

    # 测试孔位占用
    print_test "错误处理 - 孔位占用"
    payload=$(cat <<EOF
{
  "sampleName": "测试样本-孔位冲突",
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

    if [ "$http_code" = "400" ] || [ "$http_code" = "409" ]; then
        print_success "正确处理孔位占用"
        record_result "error_position_occupied" "PASS"
    else
        print_fail "未正确处理孔位占用 (HTTP $http_code)"
        record_result "error_position_occupied" "FAIL"
    fi

    # 测试必填字段
    print_test "错误处理 - 必填字段验证"
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

    if [ "$http_code" = "400" ]; then
        print_success "正确处理必填字段验证"
        record_result "error_required_field" "PASS"
    else
        print_fail "未正确处理必填字段验证 (HTTP $http_code)"
        record_result "error_required_field" "FAIL"
    fi

    # 测试无效状态转换
    if [ -n "$SAMPLE_ID_1" ]; then
        print_test "错误处理 - 无效状态转换"
        response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/samples/$SAMPLE_ID_1/status?status=invalid_status")
        http_code=$(echo "$response" | tail -n1)

        if [ "$http_code" = "400" ]; then
            print_success "正确处理无效状态"
            record_result "error_invalid_status" "PASS"
        else
            print_fail "未正确处理无效状态 (HTTP $http_code)"
            record_result "error_invalid_status" "FAIL"
        fi
    fi
}

# 测试8: CORS配置
test_cors() {
    print_header "测试8: CORS配置"

    print_test "测试CORS预检请求"
    response=$(curl -s -w "\n%{http_code}" -X OPTIONS "$BASE_URL/samples" \
        -H "Origin: http://localhost:5173" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type")
    http_code=$(echo "$response" | tail -n1)

    if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
        print_success "CORS配置正确"
        record_result "cors_config" "PASS"
    else
        print_fail "CORS配置可能有问题 (HTTP $http_code)"
        record_result "cors_config" "FAIL"
    fi
}

# 生成测试报告
generate_report() {
    print_header "测试报告"

    echo -e "\n${CYAN}测试时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}测试环境: $BASE_URL${NC}"
    echo -e "${CYAN}数据库: $DB_HOST:$DB_PORT/$DB_NAME${NC}\n"

    # 统计结果
    local pass_count=0
    local fail_count=0
    local partial_count=0

    for test_name in "${!TEST_RESULTS[@]}"; do
        result="${TEST_RESULTS[$test_name]}"
        case $result in
            PASS|EXCELLENT|GOOD)
                pass_count=$((pass_count + 1))
                ;;
            FAIL|POOR)
                fail_count=$((fail_count + 1))
                ;;
            PARTIAL|FAIR)
                partial_count=$((partial_count + 1))
                ;;
        esac
    done

    echo -e "${BLUE}测试结果统计:${NC}"
    echo -e "  总测试数: $TEST_COUNT"
    echo -e "  ${GREEN}通过: $pass_count${NC}"
    echo -e "  ${RED}失败: $fail_count${NC}"
    echo -e "  ${YELLOW}部分通过: $partial_count${NC}"

    echo -e "\n${BLUE}详细结果:${NC}"
    for test_name in "${!TEST_RESULTS[@]}"; do
        result="${TEST_RESULTS[$test_name]}"
        case $result in
            PASS|EXCELLENT|GOOD)
                echo -e "  ${GREEN}✓${NC} $test_name: $result"
                ;;
            FAIL|POOR)
                echo -e "  ${RED}✗${NC} $test_name: $result"
                ;;
            PARTIAL|FAIR)
                echo -e "  ${YELLOW}⚠${NC} $test_name: $result"
                ;;
        esac
    done

    echo ""
    if [ $fail_count -eq 0 ]; then
        echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║  ✓ 所有测试通过! 接口联调准备就绪                          ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
        return 0
    else
        echo -e "${RED}╔════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║  ✗ 有 $fail_count 个测试失败,需要修复                        ║${NC}"
        echo -e "${RED}╚════════════════════════════════════════════════════════════╝${NC}"
        return 1
    fi
}

# 主函数
main() {
    clear
    echo -e "${CYAN}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                                                            ║"
    echo "║        ArkOne Flowable API 详细测试                        ║"
    echo "║                                                            ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"

    prepare_test_env
    test_single_sample_creation
    test_sample_query
    test_sample_status_update
    test_batch_creation_small
    test_batch_creation_96
    test_field_definition
    test_error_handling
    test_cors

    generate_report
}

# 执行主函数
main
exit $?
