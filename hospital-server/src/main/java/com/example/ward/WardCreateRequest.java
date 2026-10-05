package com.example.ward;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class WardCreateRequest {

    @NotBlank(message = "病区编号不能为空")
    @Size(max = 30, message = "病区编号不能超过30个字符")
    private String wardNo;

    @NotNull(message = "所属科室不能为空")
    @Positive(message = "科室ID必须大于0")
    private Long departmentId;

    @NotBlank(message = "病区名称不能为空")
    @Size(max = 100, message = "病区名称不能超过100个字符")
    private String name;

    @NotBlank(message = "所在楼栋不能为空")
    @Size(max = 100, message = "楼栋名称不能超过100个字符")
    private String building;

    @NotBlank(message = "所在楼层不能为空")
    @Size(max = 20, message = "楼层不能超过20个字符")
    private String floorNo;

    public String getWardNo() {
        return wardNo;
    }

    public void setWardNo(String wardNo) {
        this.wardNo = wardNo;
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

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(String floorNo) {
        this.floorNo = floorNo;
    }
}