package com.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.common.PageResult;
import com.example.dto.DepartmentCreateRequest;
import com.example.dto.DepartmentUpdateRequest;
import com.example.entity.Department;

import java.util.List;


public interface DepartmentService extends IService<Department> {

    Long create(DepartmentCreateRequest request);


    default Department getDetail(Long id) {
        return null;
    }


    void delete(Long id);

    void update(Long id, DepartmentUpdateRequest request);

    PageResult<Department> pageQuery(
            long pageNo,
            long pageSize,
            String keyword,
            Integer status
    );


}