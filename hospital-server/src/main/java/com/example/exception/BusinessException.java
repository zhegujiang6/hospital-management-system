package com.example.exception;

/**
 * 业务异常类
 * 继承自RuntimeException，用于在业务逻辑处理过程中主动抛出的异常
 * 例如：挂号失败、支付异常、数据不存在等业务相关的错误场景
 * 该异常会被GlobalExceptionHandler统一捕获并处理
 */
public class BusinessException extends RuntimeException{

    /**
     * 构造业务异常
     *
     * @param message 异常描述信息，用于告知前端具体的错误原因
     */
    public BusinessException(String message) {
        super(message);
    }
}