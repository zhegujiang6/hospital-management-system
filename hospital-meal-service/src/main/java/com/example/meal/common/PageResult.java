package com.example.meal.common;

import java.util.List;

/**
 * 统一分页查询结果。
 *
 * @param <T> 每条记录的数据类型
 */
public class PageResult<T> {

    private final List<T> records;

    private final Long total;

    private final Long pageNum;

    private final Long pageSize;

    public PageResult(
            List<T> records,
            Long total,
            Long pageNum,
            Long pageSize) {

        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public Long getTotal() {
        return total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }
}