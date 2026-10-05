package com.example.patient;

import com.example.common.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 提供给其他微服务调用的患者内部接口。
 */
@RestController
@RequestMapping("/internal/patients")
public class InternalPatientController {

    private final PatientMealInfoService
            patientMealInfoService;

    public InternalPatientController(
            PatientMealInfoService
                    patientMealInfoService) {

        this.patientMealInfoService =
                patientMealInfoService;
    }

    /**
     * 查询点餐业务需要的患者状态和住院位置。
     *
     * 患者只能查询自己的信息，
     * 管理员可以查询任意患者。
     */
    @PreAuthorize("""
            hasRole('ADMIN')
            or (
                hasRole('PATIENT')
                and #patientId
                    == authentication.principal.patientId
            )
            """)
    @GetMapping("/{patientId}/meal-info")
    public Result<PatientMealInfoVO>
    getPatientMealInfo(
            @PathVariable
            Long patientId) {

        PatientMealInfoVO info =
                patientMealInfoService
                        .getPatientMealInfo(
                                patientId
                        );

        return Result.success(info);
    }
}