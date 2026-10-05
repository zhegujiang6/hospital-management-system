package com.example.meal.controller;

import com.example.meal.client.HospitalPatientClient;
import com.example.meal.client.vo.PatientMealInfoResponse;
import com.example.meal.common.Result;
import com.example.meal.exception.BusinessException;
import com.example.meal.security.MealLoginUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 患者在餐饮服务中使用的配送信息接口。
 */
@RestController
@RequestMapping("/meal/patient/delivery-info")
public class MealPatientInfoController {

    private final HospitalPatientClient
            hospitalPatientClient;

    public MealPatientInfoController(
            HospitalPatientClient
                    hospitalPatientClient) {

        this.hospitalPatientClient =
                hospitalPatientClient;
    }

    /**
     * 从医院主服务查询当前患者的住院配送信息。
     *
     * @param loginUser JWT验证后的当前患者
     * @return 患者状态和住院位置
     */
    @GetMapping
    public Result<PatientMealInfoResponse>
    getDeliveryInfo(
            @AuthenticationPrincipal
            MealLoginUser loginUser) {

        /*
         * patientId必须来自当前JWT，
         * 不能接收前端传入的任意患者ID。
         */
        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        /*
         * Feign根据hospital-server服务名查询Nacos，
         * 再调用医院主服务的内部接口。
         */
        Result<PatientMealInfoResponse>
                remoteResult =
                hospitalPatientClient
                        .getPatientMealInfo(
                                loginUser.getPatientId()
                        );

        /*
         * 防止远程服务返回空响应或业务失败，
         * 避免后面直接读取data时发生空指针。
         */
        if (remoteResult == null) {

            throw new BusinessException(
                    "医院主服务未返回患者信息"
            );
        }

        if (!Integer.valueOf(200).equals(
                remoteResult.getCode())) {

            throw new BusinessException(
                    remoteResult.getMessage()
            );
        }

        if (remoteResult.getData() == null) {

            throw new BusinessException(
                    "医院主服务未返回患者数据"
            );
        }

        return Result.success(
                remoteResult.getData()
        );
    }
}