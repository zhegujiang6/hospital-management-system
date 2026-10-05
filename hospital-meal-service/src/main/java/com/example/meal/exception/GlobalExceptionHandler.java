package com.example.meal.exception;

import com.example.meal.common.Result;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 统一捕获Controller和Service抛出的异常，并转换成Result返回给前端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理Service主动抛出的业务异常。
     *
     * @param exception 业务异常
     * @return 包含具体业务错误原因的结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(
            BusinessException exception) {

        return Result.error(400, exception.getMessage());
    }

    /**
     * 处理请求参数校验失败异常。
     *
     * @param exception 参数校验异常
     * @return 第一个校验失败字段的错误提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            MethodArgumentNotValidException exception) {

        FieldError fieldError =
                exception.getBindingResult().getFieldError();

        String message = fieldError == null
                ? "请求参数不正确"
                : fieldError.getDefaultMessage();

        return Result.error(400, message);
    }

    /**
     * 处理并发情况下数据库唯一索引产生的重复数据异常。
     *
     * @param exception 数据重复异常
     * @return 重复提交提示
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(
            DuplicateKeyException exception) {

        return Result.error(409, "数据已经存在，请勿重复提交");
    }

    /**
     * 处理日期、枚举或JSON格式不正确的请求。
     *
     * @param exception 请求体解析异常
     * @return 参数格式错误提示
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadableException(
            HttpMessageNotReadableException exception) {

        return Result.error(
                400,
                "请求参数格式不正确，请检查日期和餐次"
        );
    }
}