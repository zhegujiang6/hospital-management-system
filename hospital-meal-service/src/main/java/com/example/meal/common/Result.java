package com.example.meal.common;

/**
 * 统一接口返回结果。
 *
 * @param <T> 接口返回的数据类型
 */
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建操作成功的返回结果。
     *
     * @param data 返回给前端的数据
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 创建操作失败的返回结果。
     *
     * @param code 错误码
     * @param message 错误原因
     * @return 失败结果
     */
    public static <T> Result<T> error(
            Integer code,
            String message) {

        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}