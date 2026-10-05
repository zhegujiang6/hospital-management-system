package com.example.meal.client.vo;

/**
 * 餐饮服务接收的患者状态和住院位置信息。
 */
public record PatientMealInfoResponse(

        Long patientId,

        String patientName,

        String patientPhone,

        boolean patientEnabled,

        boolean currentlyAdmitted,

        String wardName,

        String building,

        String floorNo,

        String roomNo,

        String bedNo,

        String dietaryNotes

) {
}