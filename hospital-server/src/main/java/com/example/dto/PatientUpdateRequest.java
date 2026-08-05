package com.example.dto;


import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class PatientUpdateRequest {
    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 50, message = "患者姓名长度不能超过50个字符")
    private String name;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "男|女", message = "性别只能是男或女")
    private String gender;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(
            regexp = "(^\\d{15}$)|(^\\d{17}([0-9]|X|x)$)",
            message = "身份证号格式不正确"
    )
    private String idCard;

    @Pattern(
            regexp = "^$|^1\\d{10}$",
            message = "手机号格式不正确"
    )
    private String phone;

    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须早于今天")
    private LocalDate birthDate;

    @Size(max = 255, message = "家庭住址长度不能超过255个字符")
    private String address;

    @NotNull(message = "患者状态不能为空")
    @Min(value = 0, message = "患者状态只能是0或1")
    @Max(value = 1, message = "患者状态只能是0或1")
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}



