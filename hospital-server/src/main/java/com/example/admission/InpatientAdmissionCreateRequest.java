package com.example.admission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class InpatientAdmissionCreateRequest {

    @NotNull(message = "患者不能为空")
    @Positive(message = "患者ID必须大于0")
    private Long patientId;

    @NotNull(message = "床位不能为空")
    @Positive(message = "床位ID必须大于0")
    private Long bedId;

    @Size(max = 500, message = "饮食注意事项不能超过500个字符")
    private String dietaryNotes;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getDietaryNotes() {
        return dietaryNotes;
    }

    public void setDietaryNotes(String dietaryNotes) {
        this.dietaryNotes = dietaryNotes;
    }
}
