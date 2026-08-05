package com.example.dto;


import jakarta.validation.constraints.*;

public class DoctorCreateRequest {

    @NotBlank(message = "医生姓名不能为空")
    private String name;

    @NotBlank(message = "医生工号不能为空")
    @Size(max = 30, message = "医生工号长度必须为30位")
    @NotBlank(message = "医生姓名不能为空")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "医生工号只能包含字母、数字和横线"
    )
    private String doctorNo;

    @NotNull(message = "医生所属科室不能为空")
    @Positive(message = "科室id必须大于0")
    private Long departmentId;

    @NotNull(message = "医生性别不能为空")
    @Min(value = 0, message = "性别只能是0或1")
    @Max(value = 1, message = "性别只能是0或1")
    private Integer gender;

    @NotBlank(message = "医生职称不能为空")
    @Size(max = 30, message = "医生职称长度不能超过30位")
    private String title;

    @Size(max = 11, message = "手机号长度不能超过11位")
    private String phone;


    @Size(max = 255, message = "擅长领域不能超过255个字符")
    private String specialty;

    @Size(max = 500, message = "医生简介不能超过500个字符")
    private String introduction;


    public String getDoctorNo() {
        return doctorNo;
    }

    public void setDoctorNo(String doctorNo) {
        this.doctorNo = doctorNo;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

