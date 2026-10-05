package com.example.doctor;


import jakarta.validation.constraints.*;

public class DoctorUpdateRequest {

    @NotBlank(message = "医生工号不能为空")
    @Size(max = 30, message = "医生工号不能超过30个字符")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "医生工号只能包含字母、数字和横线"
    )
    private String doctorNo;

    @NotNull(message = "所属科室不能为空")
    @Positive(message = "科室ID必须大于0")
    private Long departmentId;

    @NotBlank(message = "医生姓名不能为空")
    @Size(max = 50, message = "医生姓名不能超过50个字符")
    private String name;

    @NotNull(message = "医生性别不能为空")
    @Min(value = 0, message = "医生性别只能是0或1")
    @Max(value = 1, message = "医生性别只能是0或1")
    private Integer gender;

    @NotBlank(message = "医生职称不能为空")
    @Size(max = 50, message = "医生职称不能超过50个字符")
    private String title;

    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "擅长领域不能超过255个字符")
    private String specialty;

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Size(max = 500, message = "医生简介不能超过500个字符")
    private String introduction;

    @NotNull(message = "医生状态不能为空")
    @Min(value = 0, message = "医生状态只能是0或1")
    @Max(value = 1, message = "医生状态只能是0或1")
    private Integer status;



}
