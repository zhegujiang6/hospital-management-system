package com.example.admission;

import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface InpatientAdmissionService
        extends IService<InpatientAdmission> {

    /**
     * 为患者办理入院并占用床位。
     */
    Long admit(InpatientAdmissionCreateRequest request);
    /**
     * 查询全部住院记录。
     */
    List<InpatientAdmissionVO> listDetails();

    /**
     * 查询指定住院记录详情。
     */
    InpatientAdmissionVO getDetail(Long id);

    /**
     * 为住院患者办理出院并释放床位。
     */
    void discharge(Long id);

    /**
     * 根据患者ID查询当前生效的住院记录。
     *
     * @param patientId 患者档案ID
     * @return 当前住院详情；未住院时返回null
     */
    InpatientAdmissionVO getActiveByPatientId(
            Long patientId
    );
}
