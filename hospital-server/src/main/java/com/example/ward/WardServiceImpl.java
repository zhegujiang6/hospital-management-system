package com.example.ward;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.department.Department;
import com.example.exception.BusinessException;
import com.example.department.DepartmentService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WardServiceImpl
        extends ServiceImpl<WardMapper, Ward>
        implements WardService {

    private final DepartmentService departmentService;

    public WardServiceImpl(
            DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    @Override
    public Long create(WardCreateRequest request) {

        // 第一步：验证所属科室确实存在。
        Department department =
                departmentService.getById(
                        request.getDepartmentId()
                );

        if (department == null) {
            throw new BusinessException(
                    "所属科室不存在"
            );
        }

        // 第二步：停用的科室不能继续创建新病区。
        if (!Integer.valueOf(1).equals(
                department.getStatus())) {

            throw new BusinessException(
                    "所属科室已停用"
            );
        }

        // 统一去掉用户输入的首尾空格。
        String wardNo =
                request.getWardNo().trim();

        // 第三步：提前检查病区编号是否重复，
        // 主要用于向用户返回容易理解的错误。
        boolean exists = lambdaQuery()
                .eq(Ward::getWardNo, wardNo)
                .exists();

        if (exists) {
            throw new BusinessException(
                    "病区编号已存在"
            );
        }

        // 第四步：把请求DTO转换成数据库实体。
        Ward ward = new Ward();
        ward.setWardNo(wardNo);
        ward.setDepartmentId(
                request.getDepartmentId()
        );
        ward.setName(
                request.getName().trim()
        );
        ward.setBuilding(
                request.getBuilding().trim()
        );
        ward.setFloorNo(
                request.getFloorNo().trim()
        );
        ward.setStatus(1);

        try {
            // save最终会调用WardMapper执行INSERT。
            save(ward);
        } catch (DuplicateKeyException exception) {

            // 两个请求并发创建相同编号时，
            // 前面的exists查询可能都返回false。
            // 最终仍然由数据库唯一索引阻止重复数据。
            throw new BusinessException(
                    "病区编号已存在"
            );
        }

        // INSERT完成后，自增ID会回填到实体中。
        return ward.getId();
    }

    @Override
    public List<WardVO> listDetails() {
        return baseMapper.selectWardList();
    }
    @Override
    public WardVO getDetail(Long id) {

        WardVO ward =
                baseMapper.selectWardDetail(id);

        if (ward == null) {
            throw new BusinessException(
                    "病区不存在"
            );
        }

        return ward;
    }
}