package com.example.meal.exception;

/**
 * 业务异常。
 * 当用户操作不符合业务规则时，由Service主动抛出。
 */
public class BusinessException extends RuntimeException {

    /**
     * 根据错误原因创建业务异常。
     *
     * @param message 要返回给前端的错误提示
     */
    public BusinessException(String message) {
        super(message);
    }
}