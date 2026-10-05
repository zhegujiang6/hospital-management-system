package com.example.patient;

import com.example.admission.InpatientAdmissionService;
import com.example.admission.InpatientAdmissionVO;
import org.springframework.stereotype.Service;

/**
 * 为餐饮微服务组装患者档案和当前住院位置。
 */
@Service
public class PatientMealInfoService {

    private final PatientService patientService;

    private final InpatientAdmissionService
            inpatientAdmissionService;

    public PatientMealInfoService(
            PatientService patientService,
            InpatientAdmissionService
                    inpatientAdmissionService) {

        this.patientService = patientService;
        this.inpatientAdmissionService =
                inpatientAdmissionService;
    }

    /**
     * 查询餐饮业务需要的患者信息。
     *
     * @param patientId 患者档案ID
     * @return 患者状态和当前住院位置
     */
    public PatientMealInfoVO getPatientMealInfo(
            Long patientId) {

        /*
         * 先确认患者真实存在。
         * getDetail在患者不存在时会抛出统一业务异常。
         */
        Patient patient =
                patientService.getDetail(patientId);

        boolean patientEnabled =
                Integer.valueOf(1)
                        .equals(patient.getStatus());

        /*
         * 查询患者当前ACTIVE住院记录。
         * 未住院时会返回null。
         */
        InpatientAdmissionVO activeAdmission =
                inpatientAdmissionService
                        .getActiveByPatientId(
                                patientId
                        );

        /*
         * 患者存在但当前未住院时，
         * 返回患者基本状态，床位字段全部为空。
         */
        if (activeAdmission == null) {

            return new PatientMealInfoVO(
                    patient.getId(),
                    patient.getName(),
                    patient.getPhone(),
                    patientEnabled,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        /*
         * 患者正在住院时，
         * 返回病区、楼栋、楼层、房间和床位。
         */
        return new PatientMealInfoVO(
                patient.getId(),
                patient.getName(),
                patient.getPhone(),
                patientEnabled,
                true,
                activeAdmission.getWardName(),
                activeAdmission.getBuilding(),
                activeAdmission.getFloorNo(),
                activeAdmission.getRoomNo(),
                activeAdmission.getBedNo(),
                activeAdmission.getDietaryNotes()
        );
    }
}
