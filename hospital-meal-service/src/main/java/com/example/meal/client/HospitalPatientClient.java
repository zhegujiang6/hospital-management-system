package com.example.meal.client;

import com.example.meal.client.vo.PatientMealInfoResponse;
import com.example.meal.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 调用医院主服务的患者内部接口。
 */
@FeignClient(
        name = "hospital-server",
        path = "/internal/patients"
)
public interface HospitalPatientClient {

    /**
     * 查询患者状态和当前住院位置。
     *
     * @param patientId 患者档案ID
     * @return 医院主服务的统一响应
     */
    @GetMapping("/{patientId}/meal-info")
    Result<PatientMealInfoResponse>
    getPatientMealInfo(
            @PathVariable("patientId")
            Long patientId
    );
}