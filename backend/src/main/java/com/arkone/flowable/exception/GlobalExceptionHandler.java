package com.arkone.flowable.exception;

import com.arkone.flowable.common.ErrorCode;
import com.arkone.flowable.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一处理系统中的各类异常，将异常转换为标准的Result响应格式。</p>
 *
 * <p>处理的异常类型包括：</p>
 * <ul>
 *   <li>BusinessException: 业务异常</li>
 *   <li>MethodArgumentNotValidException: 参数验证异常（@Valid）</li>
 *   <li>BindException: 参数绑定异常</li>
 *   <li>ConstraintViolationException: 约束违反异常（@Validated）</li>
 *   <li>MissingServletRequestParameterException: 缺少请求参数异常</li>
 *   <li>HttpMessageNotReadableException: 请求体解析异常</li>
 *   <li>MethodArgumentTypeMismatchException: 参数类型不匹配异常</li>
 *   <li>HttpRequestMethodNotSupportedException: 请求方法不支持异常</li>
 *   <li>NoHandlerFoundException: 404异常</li>
 *   <li>Exception: 其他未知异常</li>
 * </ul>
 *
 * @author ArkOne Team
 * @version 1.0
 * @since 2026-02-25
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e BusinessException
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}, errorCode={}",
                e.getCode(), e.getMessage(), e.getErrorCode() != null ? e.getErrorCode().name() : "null");

        if (e.getData() != null) {
            return Result.error(e.getErrorCode() != null ? e.getErrorCode() : ErrorCode.BAD_REQUEST, e.getData());
        }

        return e.getErrorCode() != null
                ? Result.error(e.getErrorCode(), e.getMessage())
                : Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常（@Valid）
     *
     * @param e MethodArgumentNotValidException
     * @return 统一响应结果，包含所有验证失败的字段信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数验证失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return Result.error(ErrorCode.VALIDATION_FAILED, errors);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e BindException
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        log.warn("参数绑定失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return Result.error(ErrorCode.VALIDATION_FAILED, errors);
    }

    /**
     * 处理约束违反异常（@Validated）
     *
     * @param e ConstraintViolationException
     * @return 统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束验证失败: {}", e.getMessage());

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        return Result.error(ErrorCode.VALIDATION_FAILED, errors);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e MissingServletRequestParameterException
     * @return 统一响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getMessage());

        String message = String.format("缺少必需的请求参数: %s (%s)",
                e.getParameterName(), e.getParameterType());

        return Result.error(ErrorCode.REQUIRED_FIELD_MISSING, message);
    }

    /**
     * 处理请求体解析异常
     *
     * @param e HttpMessageNotReadableException
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());

        return Result.error(ErrorCode.BAD_REQUEST, "请求体格式错误或无法解析");
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e MethodArgumentTypeMismatchException
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());

        String message = String.format("参数 '%s' 的值 '%s' 类型不正确，期望类型: %s",
                e.getName(), e.getValue(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

        return Result.error(ErrorCode.INVALID_FIELD_FORMAT, message);
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e HttpRequestMethodNotSupportedException
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());

        String message = String.format("不支持 %s 请求方法，支持的方法: %s",
                e.getMethod(), String.join(", ", e.getSupportedMethods()));

        return Result.error(ErrorCode.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 处理404异常
     *
     * @param e NoHandlerFoundException
     * @return 统一响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("请求路径不存在: {}", e.getRequestURL());

        String message = String.format("请求路径 %s 不存在", e.getRequestURL());

        return Result.error(ErrorCode.NOT_FOUND, message);
    }

    /**
     * 处理非法参数异常
     *
     * @param e IllegalArgumentException
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());

        return Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理空指针异常
     *
     * @param e NullPointerException
     * @return 统一响应结果
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);

        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, "系统内部错误，请联系管理员");
    }

    /**
     * 处理其他未知异常
     *
     * @param e Exception
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);

        // 生产环境不暴露详细错误信息
        String message = "系统内部错误，请联系管理员";

        // 开发环境可以返回详细错误信息（可通过配置控制）
        // String message = e.getMessage();

        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
