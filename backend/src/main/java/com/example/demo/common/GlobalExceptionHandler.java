package com.example.demo.common;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常（主动抛出）
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBiz(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    // 400：请求错误（参数缺失/类型不匹配/JSON 解析失败等）
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        return ApiResponse.fail(40000, ex.getMessage());
    }

    // 404（Spring 6.1+ / Boot 3.2+ 默认抛出）
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResource(NoResourceFoundException ex) {
        return ApiResponse.fail(40400, "资源不存在：" + ex.getResourcePath());
    }

    // 404（兼容旧版本，某些场景仍可能出现）
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<Void> handleNotFound(NoHandlerFoundException ex) {
        return ApiResponse.fail(40400, "资源不存在：" + ex.getRequestURL());
    }

    // 500：兜底（未预料的异常）
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOthers(Exception ex) {
        // 生产环境建议记录日志/告警
        return ApiResponse.fail(50000, "服务器异常，请稍后再试");
    }
}

