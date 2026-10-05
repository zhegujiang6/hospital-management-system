package com.example.doctor;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.department.Department;
import com.example.exception.BusinessException;
import com.example.department.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/*
 * 医生业务实现类。
 * Controller 面向 DoctorService 接口调用，真正的新增、查询、修改、删除逻辑写在这里。
 * ServiceImpl<DoctorMapper, Doctor> 让本类可以直接通过 DoctorMapper 操作 doctor 表。
 */
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    // 保存科室业务接口，因为创建或修改医生时必须检查医生所属的科室。
    private final DepartmentService departmentService;

    // Spring 创建 DoctorServiceImpl 时，会把 DepartmentService 的实现对象传进来。
    public DoctorServiceImpl(DepartmentService departmentService){

        // 把传进来的科室服务保存到成员变量，后面的方法就能调用它。
        this.departmentService = departmentService;
    }
    // 对应 DoctorService 中的 create 方法。
    @Override
    public Long create(DoctorCreateRequest request) {
        // 从创建 DTO 取出医生工号，去掉空格并转成大写，保证工号格式统一。
        String doctorNo = request.getDoctorNo().trim().toUpperCase();
        // 查询 doctor 表，判断这个工号是否已经存在。
        boolean doctorNoExists = lambdaQuery().eq(Doctor::getDoctorNo, doctorNo).exists();

        // 如果查询结果为 true，就不能继续新增医生。
        if (doctorNoExists) {
            // 抛出异常，由统一异常处理把提示返回前端。
            throw new BusinessException("医生工号已存在");
        }
        // 调用 DepartmentService 查询 DTO 中 departmentId 对应的科室；这里关联到科室模块。
        Department department = departmentService.getDetail(request.getDepartmentId());

        // getDetail 已保证科室存在，这里继续检查科室是否停用。
        if (department.getStatus() == 0) {
            // 科室停用时不允许把新医生放进这个科室。
            throw new com.example.exception.BusinessException("科室已停用");
        }

        // 创建 Doctor 实体对象，它对应 doctor 表中准备新增的一行。
        Doctor doctor = new Doctor();

        // 把整理过的医生工号放进实体，对应 doctor_no 字段。
        doctor.setDoctorNo(doctorNo);
        // 把 DTO 中的科室 id 放进医生实体，建立医生与科室的关联。
        doctor.setDepartmentId(request.getDepartmentId());
        // 把 DTO 中的姓名去掉首尾空格后放进实体。
        doctor.setName(request.getName().trim());
        // 把 DTO 中的性别放进实体。
        doctor.setGender(request.getGender());
        // 把 DTO 中的职称去掉首尾空格后放进实体。
        doctor.setTitle(request.getTitle().trim());
        // 把 DTO 中的手机号放进实体。
        doctor.setPhone(request.getPhone());
        // 把 DTO 中的擅长方向放进实体。
        doctor.setSpecialty(request.getSpecialty());
        // 把 DTO 中的医生简介放进实体。
        doctor.setIntroduction(request.getIntroduction());
        // 新建医生默认启用，1 表示启用。
        doctor.setStatus(1);

        // save 来自 ServiceImpl，内部通过 DoctorMapper 向 doctor 表插入数据。
        save(doctor);

        // 数据库生成 id 后会回填到 doctor 实体，这里把新医生 id 返回给 Controller。
        return doctor.getId();
    }

    // 对应 DoctorService 中的 listDetails，查询带科室名称的医生列表。
    @Override
    public List<DoctorVO> listDetails() {
        // baseMapper 就是 DoctorMapper；调用其中的多表联查 SQL，返回 DoctorVO 列表。
        return baseMapper.selectDoctorList();
    }


    // 对应 DoctorService 中的 getDetail，查询一名医生的详细信息。
    @Override
    public DoctorVO getDetail(Long id) {
        // 调用 DoctorMapper 的多表联查，根据医生 id 同时查询医生和科室信息。
        DoctorVO doctorVO = baseMapper.selectDoctorDetail(id);

        // Mapper 没查到数据时会返回 null。
        if (doctorVO == null) {
            // 停止执行，并把“医生不存在”返回给调用方。
            throw new BusinessException("医生不存在");
        }

        // 查询成功后返回 VO；VO 通常会被 Controller 返回给前端。
        return doctorVO;
    }

    // 对应 DoctorService 中的 delete 方法。
    @Override
    public void delete(Long id) {
        // 先通过 DoctorMapper 查询 doctor 表，确认医生存在。
        Doctor doctor = getById(id);

        // doctor 为 null 表示没有查到这个 id。
        if (doctor == null) {
            // 不继续删除，直接抛出业务异常。
            throw new BusinessException("医生不存在");
        }
        // 确认存在后，根据 id 删除 doctor 表中的记录。
        removeById(id);
    }

    // 对应 DoctorService 中的 update；id 决定修改哪名医生，request 提供新数据。
    @Override
    public void update(Long id,DoctorUpdateRequest request) {
        // 根据 id 查询原来的 Doctor 实体。
        Doctor doctor = getById(id);
        // 如果没有查询到医生，就不能继续修改。
        if (doctor == null) {
            // 抛出异常并把提示返回前端。
            throw new BusinessException("医生不存在");
        }
        // 从修改 DTO 中取出工号，去空格并转成大写。
        String doctorNo = request.getDoctorNo().trim().toUpperCase();
        // 查询是否有其他医生使用这个工号；ne(id) 用来排除当前医生自己。
        boolean doctorNoExists = lambdaQuery().eq(Doctor::getDoctorNo, doctorNo).ne(Doctor::getId, id).exists();
        // true 表示工号被其他医生占用。
        if (doctorNoExists) {
            // 不允许重复工号。
            throw new BusinessException("医生工号已存在");
        }
        // 调用科室模块，确认修改后的 departmentId 对应的科室存在。
        Department department = departmentService.getDetail(request.getDepartmentId());
        // 再检查这个科室是否处于停用状态。
        if (department.getStatus() == 0) {
            // 停用科室不能接收医生。
            throw new BusinessException("科室已停用");

        }

        // 把新工号写入原 Doctor 实体。
        doctor.setDoctorNo(doctorNo);
        // 更新医生所属科室 id。
        doctor.setDepartmentId(request.getDepartmentId());
        // 更新医生姓名。
        doctor.setName(request.getName().trim());
        // 更新医生性别。
        doctor.setGender(request.getGender());
        // 更新医生职称。
        doctor.setTitle(request.getTitle().trim());
        // 更新医生电话。
        doctor.setPhone(request.getPhone());
        // 更新医生擅长方向。
        doctor.setSpecialty(request.getSpecialty());
        // 更新医生介绍。
        doctor.setIntroduction(request.getIntroduction());
        // 更新医生启用或停用状态。
        doctor.setStatus(request.getStatus());
        // updateById 内部调用 DoctorMapper，根据 doctor.id 更新 doctor 表。
        updateById(doctor);
    }





}
