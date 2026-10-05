package com.example.department;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.common.PageResult;

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