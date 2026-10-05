package com.example.visitrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VisitRecordCreateRequest {

    @NotNull(message = "挂号订单ID不能为空")
    private Long registrationOrderId;

    @NotBlank(message = "患者主诉不能为空")
    @Size(
            max = 500,
            message = "患者主诉不能超过500个字符"
    )
    private String chiefComplaint;

    @Size(
            max = 1000,
            message = "现病史不能超过1000个字符"
    )
    private String presentIllness;

    @NotBlank(message = "诊断结果不能为空")
    @Size(
            max = 500,
            message = "诊断结果不能超过500个字符"
    )
    private String diagnosis;

    @Size(
            max = 1000,
            message = "治疗方案不能超过1000个字符"
    )
    private String treatmentPlan;

    @Size(
            max = 1000,
            message = "医生嘱咐不能超过1000个字符"
    )
    private String doctorAdvice;

    public Long getRegistrationOrderId() {
        return registrationOrderId;
    }

    public void setRegistrationOrderId(Long registrationOrderId) {
        this.registrationOrderId = registrationOrderId;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getPresentIllness() {
        return presentIllness;
    }

    public void setPresentIllness(String presentIllness) {
        this.presentIllness = presentIllness;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getDoctorAdvice() {
        return doctorAdvice;
    }

    public void setDoctorAdvice(String doctorAdvice) {
        this.doctorAdvice = doctorAdvice;
    }
}