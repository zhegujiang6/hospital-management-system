package com.example.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public class PageResult<T> {

    // 当前页的数据
    private List<T> records;

    // 符合条件的数据总数
    private long total;

    // 当前页码
    private long current;

    // 每页数量
    private long size;

    // 总页数
    private long pages;

    public static <T> PageResult<T> from(
            IPage<T> page) {

        PageResult<T> result =
                new PageResult<>();

        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());

        return result;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}