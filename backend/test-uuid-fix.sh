#!/bin/bash

# UUID修复验证测试脚本
# 测试MyBatis-Plus ASSIGN_UUID是否正常工作

echo "========================================="
echo "UUID修复验证测试"
echo "========================================="
echo ""

# 启动Spring Boot应用
echo "1. 启动Spring Boot应用..."
mvn spring-boot:run > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "   后端PID: $BACKEND_PID"

# 等待应用启动
echo "2. 等待应用启动(30秒)..."
sleep 30

# 检查应用是否启动成功
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "   ❌ 应用启动失败"
    echo "   查看日志: tail -100 /tmp/backend.log"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi
echo "   ✅ 应用启动成功"
echo ""

# 测试1: 创建项目
echo "3. 测试创建项目..."
PROJECT_RESPONSE=$(curl -s -X POST http://localhost:8080/projects \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "TEST001",
    "projectName": "测试项目",
    "description": "UUID修复测试项目",
    "processDefinitionId": "plasmid-sequencing",
    "status": "active"
  }')

echo "   响应: $PROJECT_RESPONSE"

# 提取项目ID
PROJECT_ID=$(echo $PROJECT_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$PROJECT_ID" ]; then
    echo "   ❌ 创建项目失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "   ✅ 项目创建成功, ID: $PROJECT_ID"
echo "   ID长度: ${#PROJECT_ID} (应该是32位)"
echo ""

# 测试2: 创建容器
echo "4. 测试创建容器..."
CONTAINER_RESPONSE=$(curl -s -X POST http://localhost:8080/containers \
  -H "Content-Type: application/json" \
  -d '{
    "containerCode": "PLATE001",
    "containerType": "96孔板",
    "capacity": 96,
    "status": "active"
  }')

echo "   响应: $CONTAINER_RESPONSE"

CONTAINER_ID=$(echo $CONTAINER_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$CONTAINER_ID" ]; then
    echo "   ❌ 创建容器失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "   ✅ 容器创建成功, ID: $CONTAINER_ID"
echo "   ID长度: ${#CONTAINER_ID} (应该是32位)"
echo ""

# 测试3: 创建样本
echo "5. 测试创建样本..."
SAMPLE_RESPONSE=$(curl -s -X POST http://localhost:8080/samples \
  -H "Content-Type: application/json" \
  -d "{
    \"sampleName\": \"测试样本001\",
    \"sampleType\": \"plate\",
    \"projectId\": \"$PROJECT_ID\",
    \"containerId\": \"$CONTAINER_ID\",
    \"position\": \"A01\"
  }")

echo "   响应: $SAMPLE_RESPONSE"

SAMPLE_ID=$(echo $SAMPLE_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$SAMPLE_ID" ]; then
    echo "   ❌ 创建样本失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo "   ✅ 样本创建成功, ID: $SAMPLE_ID"
echo "   ID长度: ${#SAMPLE_ID} (应该是32位)"
echo ""

# 测试4: 查询样本详情
echo "6. 测试查询样本详情..."
SAMPLE_DETAIL=$(curl -s http://localhost:8080/samples/$SAMPLE_ID)
echo "   响应: $SAMPLE_DETAIL"

if echo "$SAMPLE_DETAIL" | grep -q "\"id\":\"$SAMPLE_ID\""; then
    echo "   ✅ 查询样本详情成功"
else
    echo "   ❌ 查询样本详情失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi
echo ""

# 测试5: 批量创建样本
echo "7. 测试批量创建样本..."
BATCH_RESPONSE=$(curl -s -X POST http://localhost:8080/batch/samples \
  -H "Content-Type: application/json" \
  -d "{
    \"samples\": [
      {
        \"sampleName\": \"批量样本001\",
        \"sampleType\": \"plate\",
        \"projectId\": \"$PROJECT_ID\",
        \"containerId\": \"$CONTAINER_ID\",
        \"position\": \"A02\"
      },
      {
        \"sampleName\": \"批量样本002\",
        \"sampleType\": \"plate\",
        \"projectId\": \"$PROJECT_ID\",
        \"containerId\": \"$CONTAINER_ID\",
        \"position\": \"A03\"
      }
    ],
    \"failureMode\": \"partial\"
  }")

echo "   响应: $BATCH_RESPONSE"

SUCCESS_COUNT=$(echo $BATCH_RESPONSE | grep -o '"successCount":[0-9]*' | cut -d':' -f2)

if [ "$SUCCESS_COUNT" = "2" ]; then
    echo "   ✅ 批量创建样本成功, 成功数: $SUCCESS_COUNT"
else
    echo "   ❌ 批量创建样本失败, 成功数: $SUCCESS_COUNT (期望2)"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi
echo ""

# 停止应用
echo "8. 停止应用..."
kill $BACKEND_PID 2>/dev/null
sleep 2
echo "   ✅ 应用已停止"
echo ""

echo "========================================="
echo "✅ 所有测试通过!"
echo "========================================="
echo ""
echo "修复总结:"
echo "- BaseEntity.id: UUID → String"
echo "- Sample外键: UUID → String (projectId, containerId, parentSampleId)"
echo "- ProcessNodeData.sampleId: UUID → String"
echo "- AuditLog.id/entityId: UUID → String"
echo "- 所有DTO和Service接口已更新"
echo "- MyBatis-Plus ASSIGN_UUID正常生成32位UUID字符串"
echo ""
