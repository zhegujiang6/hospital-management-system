package com.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentUpdateRequest {

    @NotBlank(message = "科室名称不能为空")
    @Size(max = 50, message = "科室名称长度不能超过50个字符")
    private String name;


    @Size(max = 255, message = "科室描述长度不能超过255个字符")
    private String description;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotBlank(message = "科室状态不能为空")
    @Min(value = 0, message = "科室状态只能是0或1")
    @Max(value = 1, message = "科室状态只能是0或1")
    private Integer status;
}
