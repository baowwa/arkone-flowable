package com.arkone.flowable.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一响应结果封装类
 *
 * <p>用于封装所有API接口的响应数据，提供统一的响应格式。</p>
 *
 * <p>响应格式示例：</p>
 * <pre>
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {},
 *   "timestamp": "2026-02-25T16:30:00"
 * }
 * </pre>
 *
 * @param <T> 响应数据的类型
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应状态码
     * <ul>
     *   <li>200: 成功</li>
     *   <li>400: 请求参数错误</li>
     *   <li>401: 未认证</li>
     *   <li>403: 无权限</li>
     *   <li>404: 资源不存在</li>
     *   <li>409: 资源冲突</li>
     *   <li>500: 服务器内部错误</li>
     * </ul>
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 业务错误码（可选）
     * <p>用于更细粒度的错误标识，如：SAMPLE_NOT_FOUND, DUPLICATE_SAMPLE_NAME等</p>
     */
    private String errorCode;

    /**
     * 构造成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 构造成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构造成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 构造失败响应（使用ErrorCode枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setErrorCode(errorCode.name());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 构造失败响应（使用ErrorCode枚举和自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param <T>       数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        result.setErrorCode(errorCode.name());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 构造失败响应（使用ErrorCode枚举和数据）
     *
     * @param errorCode 错误码枚举
     * @param data      错误详情数据
     * @param <T>       数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(ErrorCode errorCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setErrorCode(errorCode.name());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 构造失败响应（自定义状态码和消息）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    /**
     * 判断响应是否成功
     *
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return ErrorCode.SUCCESS.getCode().equals(this.code);
    }
}
