package com.example.meal.service;

import com.example.meal.client.HospitalPatientClient;
import com.example.meal.client.vo.PatientMealInfoResponse;
import com.example.meal.common.Result;
import com.example.meal.dto.MealOrderCreateRequest;
import com.example.meal.enums.MealDeliveryType;
import com.example.meal.exception.BusinessException;
import com.example.meal.service.model.ResolvedMealDelivery;
import com.example.meal.vo.MealOrderCreateVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验患者状态，并生成可以写入订单的可信配送信息。
 */
@Service
public class MealOrderPlacementService {

    private final HospitalPatientClient
            hospitalPatientClient;

    private final MealOrderService mealOrderService;

    public MealOrderPlacementService(
            HospitalPatientClient
                    hospitalPatientClient,MealOrderService mealOrderService) {

        this.hospitalPatientClient =
                hospitalPatientClient;

        this.mealOrderService =
                mealOrderService;
    }

    /**
     * 先在事务外解析配送信息，
     * 再调用事务订单Service创建订单。
     */
    public MealOrderCreateVO placeOrder(
            Long patientId,
            MealOrderCreateRequest request) {

        /*
         * 当前还没有进入MealOrderService的事务，
         * 因此Feign等待不会占用餐饮数据库事务。
         */
        ResolvedMealDelivery delivery =
                resolve(
                        patientId,
                        request
                );

        /*
         * 调用另一个Spring Bean后，
         * 才进入带有@Transactional的createOrder。
         */
        return mealOrderService.createOrder(
                patientId,
                request,
                delivery
        );
    }

    /**
     * 根据配送类型解析可信配送信息。
     *
     * @param patientId 当前JWT中的患者ID
     * @param request 前端提交的订单参数
     * @return 后端校验后的配送信息
     */
    private ResolvedMealDelivery resolve(
            Long patientId,
            MealOrderCreateRequest request) {

        PatientMealInfoResponse patientInfo =
                queryPatientInfo(patientId);

        if (!patientInfo.patientEnabled()) {

            throw new BusinessException(
                    "患者档案已停用，无法创建订单"
            );
        }

        /*
         * WARD表示配送到住院床位。
         * 地址必须使用医院主服务的真实数据。
         */
        if (MealDeliveryType.WARD
                .equals(request.getDeliveryType())) {

            return resolveWardDelivery(
                    patientInfo,
                    request
            );
        }

        /*
         * DEPARTMENT和OTHER目前仍使用用户填写的信息，
         * 但要经过后端整理和非空校验。
         */
        return resolveManualDelivery(request);
    }

    /**
     * 通过Feign查询医院主服务，并检查统一响应。
     */
    private PatientMealInfoResponse queryPatientInfo(
            Long patientId) {

        Result<PatientMealInfoResponse>
                remoteResult =
                hospitalPatientClient
                        .getPatientMealInfo(patientId);

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

        return remoteResult.getData();
    }

    /**
     * 使用患者当前真实床位生成病房配送信息。
     */
    private ResolvedMealDelivery resolveWardDelivery(
            PatientMealInfoResponse patientInfo,
            MealOrderCreateRequest request) {

        if (!patientInfo.currentlyAdmitted()) {

            throw new BusinessException(
                    "当前患者未住院，无法配送到病房"
            );
        }

        /*
         * 病区、房间和床位是完成病房配送的必要信息。
         */
        if (!StringUtils.hasText(
                patientInfo.wardName())
                || !StringUtils.hasText(
                patientInfo.roomNo())
                || !StringUtils.hasText(
                patientInfo.bedNo())) {

            throw new BusinessException(
                    "患者住院床位信息不完整"
            );
        }

        String deliveryLocation =
                buildWardLocation(patientInfo);

        /*
         * 收餐人姓名来自医院患者档案。
         * 联系电话允许患者本次下单时填写备用号码；
         * 未填写时使用患者档案中的电话。
         */
        String recipientPhone =
                StringUtils.hasText(
                        request.getRecipientPhone())
                        ? request.getRecipientPhone()
                        .trim()
                        : normalizeOptional(
                        patientInfo.patientPhone()
                );

        return new ResolvedMealDelivery(
                patientInfo.patientName(),
                recipientPhone,
                deliveryLocation
        );
    }

    /**
     * 整理科室配送或其他配送的手工信息。
     */
    private ResolvedMealDelivery
    resolveManualDelivery(
            MealOrderCreateRequest request) {

        if (!StringUtils.hasText(
                request.getRecipientName())) {

            throw new BusinessException(
                    "收餐人姓名不能为空"
            );
        }

        if (!StringUtils.hasText(
                request.getDeliveryLocation())) {

            throw new BusinessException(
                    "配送位置不能为空"
            );
        }

        return new ResolvedMealDelivery(
                request.getRecipientName().trim(),
                normalizeOptional(
                        request.getRecipientPhone()
                ),
                request.getDeliveryLocation().trim()
        );
    }

    /**
     * 将楼栋、楼层、病区、房间和床位组合成配送地址。
     */
    private String buildWardLocation(
            PatientMealInfoResponse patientInfo) {

        List<String> locationParts =
                new ArrayList<>();

        addIfPresent(
                locationParts,
                patientInfo.building()
        );

        if (StringUtils.hasText(
                patientInfo.floorNo())) {

            locationParts.add(
                    patientInfo.floorNo().trim()
                            + "层"
            );
        }

        addIfPresent(
                locationParts,
                patientInfo.wardName()
        );

        locationParts.add(
                patientInfo.roomNo().trim()
                        + "病房"
        );

        locationParts.add(
                patientInfo.bedNo().trim()
                        + "床"
        );

        return String.join(
                " ",
                locationParts
        );
    }

    /**
     * 非空文本去除首尾空格后加入地址。
     */
    private void addIfPresent(
            List<String> values,
            String value) {

        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    /**
     * 整理允许为空的文本。
     */
    private String normalizeOptional(
            String value) {

        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}