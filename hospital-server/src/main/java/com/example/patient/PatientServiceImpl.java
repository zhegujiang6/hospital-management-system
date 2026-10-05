package com.example.patient;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
/*
 * 患者业务实现类。
 * PatientController 调用 PatientService，最终会执行这里的方法。
 * ServiceImpl<PatientMapper, Patient> 提供了操作 patient 表的通用方法。
 */
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {


    // 对应 PatientService 中的 create，用来建立患者档案。
    @Override
    public Long create(PatientCreateRequest request) {
        // 从创建 DTO 取出身份证号，去空格并转为大写，保证数据库中的格式统一。
        String idCard = request.getIdCard().trim().toUpperCase();

        // 使用 PatientMapper 查询 patient 表，判断身份证号是否已经存在。
        boolean idCardExists = lambdaQuery()
                // 查询条件：数据库 id_card 等于刚才整理好的 idCard。
                .eq(Patient::getIdCard, idCard)
                // 不需要查出整条记录，只判断是否存在。
                .exists();

        // true 表示这个身份证号已经有患者档案。
        if (idCardExists) {
            // 停止创建，避免同一个人重复建档。
            throw new BusinessException("该身份证号已经建立患者档案");
        }

        // 创建 Patient 实体，它对应 patient 表中准备新增的一行。
        Patient patient = new Patient();

        // 调用本类的 generatePatientNo，生成系统内部使用的患者编号。
        patient.setPatientNo(generatePatientNo());
        // 把 DTO 中的姓名去掉首尾空格后放进实体。
        patient.setName(request.getName().trim());
        // 把性别放进实体。
        patient.setGender(request.getGender());
        // 把整理后的身份证号放进实体。
        patient.setIdCard(idCard);
        // 把手机号放进实体。
        patient.setPhone(request.getPhone());
        // 把出生日期放进实体。
        patient.setBirthDate(request.getBirthDate());
        // 把地址放进实体。
        patient.setAddress(request.getAddress());
        // 新建患者档案默认启用。
        patient.setStatus(1);

        // save 内部调用 PatientMapper，把 Patient 实体插入 patient 表。
        save(patient);

        // 插入后数据库 id 会回填到实体，这里返回给 Controller。
        return patient.getId();
    }

    // 这是本类内部使用的辅助方法，不在 PatientService 接口中，所以不需要 @Override。
    private String generatePatientNo() {
        // UUID 先生成一段几乎不会重复的随机文本。
        String randomText = UUID.randomUUID()
                // UUID 原本是对象，toString 把它变成普通字符串。
                .toString()
                // 去掉字符串中的短横线。
                .replace("-", "")
                // 只取前 12 个字符，避免患者编号过长。
                .substring(0, 12)
                // 统一转成大写。
                .toUpperCase();

        // 在随机文本前加 P，组成最终患者编号，例如 P12AB34CD56EF。
        return "P" + randomText;
    }

    // 对应 PatientService 中的 listPatients，返回患者列表。
    @Override
    public List<Patient> listPatients() {
        // lambdaQuery 会通过 PatientMapper 查询 patient 表。
        return lambdaQuery()
                // 按创建时间倒序排列，让最新建档的患者显示在前面。
                .orderByDesc(Patient::getCreateTime)
                // 执行查询并返回 List<Patient>。
                .list();
    }

    // 对应 PatientService 中的 getDetail，查询一个患者。
    @Override
    public Patient getDetail(Long id) {
        // 根据 id 查询 patient 表，结果放进 Patient 实体。
        Patient patient = getById(id);

        // patient 为 null 表示数据库没有这个患者。
        if (patient == null) {
            // 停止后续业务并返回提示。
            throw new BusinessException("患者不存在");
        }

        // 查询成功，把实体返回给 Controller 或其他 Service。
        return patient;
    }

    // 对应 PatientService 中的 update。
    @Override
    public void update(Long id, PatientUpdateRequest request) {
        // 复用本类 getDetail 查询患者；它还会统一处理“患者不存在”的情况。
        Patient patient = getDetail(id);

        // 从修改 DTO 取出身份证号。
        String idCard = request.getIdCard()
                // 去掉身份证号首尾空格。
                .trim()
                // 字母统一转成大写。
                .toUpperCase();

        // 查询是否有其他患者使用这个身份证号。
        boolean idCardExists = lambdaQuery()
                // 数据库 id_card 必须等于新的身份证号。
                .eq(Patient::getIdCard, idCard)
                // 排除当前正在修改的患者自己。
                .ne(Patient::getId, id)
                // 只返回“是否存在”。
                .exists();

        // true 表示身份证号属于另一份患者档案。
        if (idCardExists) {
            // 阻止重复身份证号写入数据库。
            throw new BusinessException("该身份证号已经建立患者档案");
        }

        // 把 DTO 中的新姓名写入查询出来的 Patient 实体。
        patient.setName(request.getName().trim());
        // 更新性别。
        patient.setGender(request.getGender());
        // 更新身份证号。
        patient.setIdCard(idCard);
        // 更新手机号。
        patient.setPhone(request.getPhone());
        // 更新出生日期。
        patient.setBirthDate(request.getBirthDate());
        // 更新家庭地址。
        patient.setAddress(request.getAddress());
        // 更新启用或停用状态。
        patient.setStatus(request.getStatus());

        // 根据 patient.id 调用 PatientMapper，真正更新 patient 数据库表。
        updateById(patient);
    }

    // 对应 PatientService 中的 delete。
    @Override
    public void delete(Long id) {
        // 先调用 getDetail 查询患者；如果不存在，它会直接抛出异常。
        Patient patient = getDetail(id);
        // 使用查到的患者 id，通过 PatientMapper 删除 patient 表记录。
        removeById(patient.getId());
    }
}




