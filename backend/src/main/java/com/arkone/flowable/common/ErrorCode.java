package com.arkone.flowable.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 *
 * <p>定义系统中所有的错误码，包括HTTP标准错误码和业务自定义错误码。</p>
 *
 * <p>错误码分类：</p>
 * <ul>
 *   <li>2xx: 成功状态</li>
 *   <li>4xx: 客户端错误</li>
 *   <li>5xx: 服务器错误</li>
 *   <li>业务错误码: 使用语义化命名，如SAMPLE_NOT_FOUND</li>
 * </ul>
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== HTTP标准错误码 ====================

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问该资源"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "请求的资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 资源冲突
     */
    CONFLICT(409, "资源冲突"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // ==================== 样本管理相关错误码 ====================

    /**
     * 样本不存在
     */
    SAMPLE_NOT_FOUND(404, "样本不存在"),

    /**
     * 样本名称重复
     */
    DUPLICATE_SAMPLE_NAME(409, "样本名称已存在"),

    /**
     * 样本状态不合法
     */
    INVALID_SAMPLE_STATUS(400, "样本状态不合法"),

    /**
     * 样本编号已存在
     */
    DUPLICATE_SAMPLE_CODE(409, "样本编号已存在"),

    // ==================== 任务管理相关错误码 ====================

    /**
     * 任务不存在
     */
    TASK_NOT_FOUND(404, "任务不存在"),

    /**
     * 任务已完成
     */
    TASK_ALREADY_COMPLETED(409, "任务已完成，无法重复操作"),

    /**
     * 任务未完成
     */
    TASK_NOT_COMPLETED(400, "任务尚未完成"),

    // ==================== 数据验证相关错误码 ====================

    /**
     * 数据验证失败
     */
    VALIDATION_FAILED(400, "数据验证失败"),

    /**
     * 必填字段缺失
     */
    REQUIRED_FIELD_MISSING(400, "必填字段缺失"),

    /**
     * 字段格式错误
     */
    INVALID_FIELD_FORMAT(400, "字段格式错误"),

    /**
     * 字段值超出范围
     */
    FIELD_VALUE_OUT_OF_RANGE(400, "字段值超出允许范围"),

    // ==================== 容器管理相关错误码 ====================

    /**
     * 容器不存在
     */
    CONTAINER_NOT_FOUND(404, "容器不存在"),

    /**
     * 孔位已被占用
     */
    POSITION_OCCUPIED(409, "孔位已被占用"),

    /**
     * 孔位不存在
     */
    POSITION_NOT_FOUND(404, "孔位不存在"),

    /**
     * 容器已满
     */
    CONTAINER_FULL(409, "容器已满，无可用孔位"),

    // ==================== 项目管理相关错误码 ====================

    /**
     * 项目不存在
     */
    PROJECT_NOT_FOUND(404, "项目不存在"),

    /**
     * 项目编码已存在
     */
    DUPLICATE_PROJECT_CODE(409, "项目编码已存在"),

    // ==================== 批量操作相关错误码 ====================

    /**
     * 批量操作失败
     */
    BATCH_OPERATION_FAILED(400, "批量操作失败"),

    /**
     * 批量操作部分失败
     */
    BATCH_OPERATION_PARTIAL_FAILED(207, "批量操作部分失败"),

    // ==================== 流程管理相关错误码 ====================

    /**
     * 流程实例不存在
     */
    PROCESS_INSTANCE_NOT_FOUND(404, "流程实例不存在"),

    /**
     * 流程定义不存在
     */
    PROCESS_DEFINITION_NOT_FOUND(404, "流程定义不存在"),

    /**
     * 流程已结束
     */
    PROCESS_ALREADY_ENDED(409, "流程已结束"),

    /**
     * 流程启动失败
     */
    PROCESS_START_FAILED(500, "流程启动失败"),

    // ==================== 公式计算相关错误码 ====================

    /**
     * 公式计算错误
     */
    FORMULA_CALCULATION_ERROR(500, "公式计算错误"),

    /**
     * 公式语法错误
     */
    FORMULA_SYNTAX_ERROR(400, "公式语法错误"),

    /**
     * 公式变量未定义
     */
    FORMULA_VARIABLE_UNDEFINED(400, "公式中使用了未定义的变量"),

    /**
     * 除数为零
     */
    DIVISION_BY_ZERO(400, "除数不能为零"),

    // ==================== 文件操作相关错误码 ====================

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(500, "文件上传失败"),

    /**
     * 文件不存在
     */
    FILE_NOT_FOUND(404, "文件不存在"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(400, "文件类型不支持"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(400, "文件大小超出限制"),

    // ==================== 权限相关错误码 ====================

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(404, "用户不存在"),

    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS(401, "用户名或密码错误"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),

    /**
     * Token无效
     */
    INVALID_TOKEN(401, "无效的Token"),

    // ==================== 数据库操作相关错误码 ====================

    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(500, "数据库操作失败"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(409, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(404, "数据不存在"),

    /**
     * 外键约束违反
     */
    FOREIGN_KEY_VIOLATION(400, "存在关联数据，无法删除"),

    // ==================== 业务规则相关错误码 ====================

    /**
     * 业务规则校验失败
     */
    BUSINESS_RULE_VIOLATION(400, "业务规则校验失败"),

    /**
     * 操作不允许
     */
    OPERATION_NOT_ALLOWED(403, "当前状态不允许该操作"),

    /**
     * 数据状态异常
     */
    INVALID_DATA_STATE(400, "数据状态异常");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 根据错误码获取ErrorCode枚举
     *
     * @param code 错误码
     * @return ErrorCode枚举，如果未找到则返回null
     */
    public static ErrorCode getByCode(Integer code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 判断是否为成功状态
     *
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为客户端错误（4xx）
     *
     * @return true表示客户端错误
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否为服务器错误（5xx）
     *
     * @return true表示服务器错误
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
}
