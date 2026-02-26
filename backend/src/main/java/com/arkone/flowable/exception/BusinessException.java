package com.arkone.flowable.exception;

import com.arkone.flowable.common.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 *
 * <p>用于封装业务逻辑中的异常情况，携带错误码和错误消息。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 使用ErrorCode枚举
 * throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND);
 *
 * // 使用ErrorCode枚举和自定义消息
 * throw new BusinessException(ErrorCode.SAMPLE_NOT_FOUND, "样本ID: " + sampleId + " 不存在");
 *
 * // 使用自定义错误码和消息
 * throw new BusinessException(404, "样本不存在");
 * </pre>
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 错误码枚举
     */
    private final ErrorCode errorCode;

    /**
     * 额外的错误详情数据
     */
    private Object data;

    /**
     * 构造函数 - 使用ErrorCode枚举
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errorCode = errorCode;
    }

    /**
     * 构造函数 - 使用ErrorCode枚举和自定义消息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
        this.errorCode = errorCode;
    }

    /**
     * 构造函数 - 使用ErrorCode枚举、自定义消息和原因异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param cause     原因异常
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.message = message;
        this.errorCode = errorCode;
    }

    /**
     * 构造函数 - 使用ErrorCode枚举和原因异常
     *
     * @param errorCode 错误码枚举
     * @param cause     原因异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errorCode = errorCode;
    }

    /**
     * 构造函数 - 使用自定义错误码和消息
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.errorCode = ErrorCode.getByCode(code);
    }

    /**
     * 构造函数 - 使用自定义错误码、消息和原因异常
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.errorCode = ErrorCode.getByCode(code);
    }

    /**
     * 设置额外的错误详情数据
     *
     * @param data 错误详情数据
     * @return 当前BusinessException实例，支持链式调用
     */
    public BusinessException withData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 静态工厂方法 - 创建BusinessException
     *
     * @param errorCode 错误码枚举
     * @return BusinessException实例
     */
    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     * 静态工厂方法 - 创建BusinessException（带自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @return BusinessException实例
     */
    public static BusinessException of(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    /**
     * 静态工厂方法 - 创建BusinessException（带自定义消息和原因异常）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param cause     原因异常
     * @return BusinessException实例
     */
    public static BusinessException of(ErrorCode errorCode, String message, Throwable cause) {
        return new BusinessException(errorCode, message, cause);
    }

    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", errorCode=" + (errorCode != null ? errorCode.name() : "null") +
                ", data=" + data +
                '}';
    }
}
