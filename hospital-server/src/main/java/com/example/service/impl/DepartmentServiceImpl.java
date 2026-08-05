package com.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.common.PageResult;
import com.example.dto.DepartmentCreateRequest;
import com.example.dto.DepartmentUpdateRequest;
import com.example.entity.Department;
import com.example.exception.BusinessException;
import com.example.mapper.DepartmentMapper;
import com.example.service.DepartmentService;
import org.springframework.stereotype.Service;


@Service
/*
 * 科室业务的具体实现类。
 * Controller 调用 DepartmentService 接口，实际执行的是这个类中的方法。
 * 继承 ServiceImpl 后，可以直接使用 save、getById、updateById、removeById 和 lambdaQuery。
 * 这些方法最终都会通过 DepartmentMapper 操作 department 数据库表。
 */
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    // @Override 表示这个 create 方法来自 DepartmentService 接口，这里负责把它真正实现出来。
    @Override
    public Long create(DepartmentCreateRequest request) {
        // request 是 Controller 接收到的 DTO；取出科室名称，去掉首尾空格后放进 name。
        String name = request.getName().trim();

        // lambdaQuery 来自 ServiceImpl；查询 department 表中是否已经存在同名科室。
        boolean exists = lambdaQuery().eq(Department::getName, name).exists();

        // 如果 exists 是 true，说明数据库已经有同名科室，立即停止创建。
        if (exists) {
            // 抛出业务异常，最后会由统一异常处理返回给前端。
            throw new BusinessException("科室名称已存在");
        }

        // 创建一个新的 Department 实体对象；它对应 department 表中准备新增的一行。
        Department department = new Department();

        // 把 DTO 中的名称放进实体对象的 name 属性，对应数据库的 name 字段。
        department.setName(request.getName());
        // 把 DTO 中的描述放进实体对象，对应数据库的 description 字段。
        department.setDescription(request.getDescription());
        // 新建科室默认启用；1 表示启用。
        department.setStatus(1);

        // save 来自 ServiceImpl，内部调用 DepartmentMapper，把 department 插入数据库。
        save(department);
        // 插入成功后数据库生成的 id 会回填到 department 中，这里把 id 返回给 Controller。
        return department.getId();
    }


    // 对应 DepartmentService 中的 getDetail，用来查询一条科室详情。
    @Override
    public Department getDetail(Long id) {
        // getById 来自 ServiceImpl，根据前端传来的 id 查询 department 表，并返回 Department 实体。
        Department department = getById(id);

        // department 如果是 null，表示 getById 没有查到这条数据库记录。
        if (department == null) {
            // 告诉前端这个科室不存在，同时停止后面的代码。
            throw new BusinessException("科室不存在");
        }
        // 查询成功，把 Department 实体返回给调用它的 Controller 或其他 Service。
        return department;
    }


    // 对应 DepartmentService 中的 update；id 指定修改谁，request 携带新的数据。
    @Override
    public void update(Long id, DepartmentUpdateRequest request) {
        // 先根据 id 查询原来的科室，避免修改一条不存在的数据。
        Department department = getById(id);

        // 没查到就停止修改。
        if (department == null) {
            // 业务异常会被转换成统一的错误结果返回前端。
            throw new BusinessException("科室不存在");
        }

        // 从修改 DTO 中取出新名称并去掉首尾空格。
        String name = request.getName().trim();

        // 查询是否有“其他科室”使用了这个名称；ne(id) 表示排除当前正在修改的科室。
        boolean exists = lambdaQuery().eq(Department::getName, name).ne(Department::getId, id).exists();

        // 如果其他科室已经使用这个名称，就不允许修改。
        if (exists) {
            // 抛出异常，把原因返回前端。
            throw new BusinessException("科室名称已存在");
        }


        // 把 DTO 中的新名称写入刚才查询出来的 Department 实体。
        department.setName(request.getName());
        // 把新的描述写入实体。
        department.setDescription(request.getDescription());
        // 把新的启用状态写入实体。
        department.setStatus(request.getStatus());
        // updateById 根据实体的 id 调用 DepartmentMapper，真正更新 department 数据库表。
        updateById(department);

    }

    // 对应 DepartmentService 中的 delete，用来删除指定 id 的科室。
    @Override
    public void delete(Long id) {
        // 删除前先查询，确认科室确实存在。
        Department department = getById(id);

        // department 为 null 表示数据库没有这条记录。
        if (department == null) {
            // 不继续执行 removeById，直接把“科室不存在”返回给前端。
            throw new BusinessException("科室不存在");
        }

        // removeById 来自 ServiceImpl，内部通过 DepartmentMapper 删除 department 表中的记录。
        removeById(id);
    }


    @Override
    public PageResult<Department> pageQuery(
            long pageNo,
            long pageSize,
            String keyword,
            Integer status) {

        // 页码必须从1开始
        if (pageNo < 1) {
            throw new BusinessException(
                    "页码不能小于1"
            );
        }

        // 限制每页数量，避免一次查询太多
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException(
                    "每页数量必须在1到100之间"
            );
        }

        // status只能是0、1或者不传
        if (status != null
                && status != 0
                && status != 1) {

            throw new BusinessException(
                    "科室状态参数错误"
            );
        }

        // 创建数据库查询条件
        LambdaQueryWrapper<Department> queryWrapper =
                new LambdaQueryWrapper<>();

        // 整理前端传来的搜索关键字
        String text = keyword == null
                ? null
                : keyword.trim();

        // 有搜索内容时，同时搜索科室名称和科室简介
        if (text != null && !text.isEmpty()) {

            queryWrapper.and(wrapper ->
                    wrapper
                            .like(
                                    Department::getName,
                                    text
                            )
                            .or()
                            .like(
                                    Department::getDescription,
                                    text
                            )
            );
        }

        // status不为空时才加入状态条件
        if (status != null) {
            queryWrapper.eq(
                    Department::getStatus,
                    status
            );
        }

        // 最近修改的科室排在前面
        queryWrapper
                .orderByDesc(
                        Department::getUpdateTime
                )
                .orderByDesc(
                        Department::getId
                );

        // 告诉MyBatis-Plus查询第几页、每页多少条
        Page<Department> pageRequest =
                new Page<>(
                        pageNo,
                        pageSize
                );

        // page方法来自ServiceImpl，真正执行分页查询
        Page<Department> pageResult =
                page(
                        pageRequest,
                        queryWrapper
                );

        // 转换成项目自己的统一分页格式
        return PageResult.from(pageResult);
    }



}
