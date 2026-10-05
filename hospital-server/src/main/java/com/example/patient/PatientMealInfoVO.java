package com.example.patient;

/**
 * 提供给餐饮微服务的患者信息。
 *
 * 只包含点餐和病房配送需要的字段，
 * 不传递身份证号、家庭地址等无关敏感信息。
 */
public record PatientMealInfoVO(

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