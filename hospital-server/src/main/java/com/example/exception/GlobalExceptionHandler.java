package com.example.exception;


import com.example.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 使用@RestControllerAdvice统一捕获和处理Controller层抛出的各类异常，
 * 避免异常直接暴露给前端，确保返回统一格式的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 捕获业务逻辑中主动抛出的BusinessException，返回对应的错误提示信息
     *
     * @param e 业务异常对象，包含错误描述信息
     * @return 统一格式的错误响应结果，状态码为400
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理方法参数校验异常
     * 捕获使用@Valid等注解进行参数校验时抛出的MethodArgumentNotValidException，
     * 提取校验失败的字段错误信息返回给前端
     *
     * @param e 参数校验异常对象，包含校验失败的字段和错误信息
     * @return 统一格式的错误响应结果，状态码为400，附带校验错误提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }
}