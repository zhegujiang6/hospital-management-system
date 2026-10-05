package com.example.bed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class BedCreateRequest {

    @NotNull(message = "所属病区不能为空")
    @Positive(message = "病区ID必须大于0")
    private Long wardId;

    @NotBlank(message = "房间号不能为空")
    @Size(max = 30, message = "房间号不能超过30个字符")
    private String roomNo;

    @NotBlank(message = "床位号不能为空")
    @Size(max = 30, message = "床位号不能超过30个字符")
    private String bedNo;

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getBedNo() {
        return bedNo;
    }

    public void setBedNo(String bedNo) {
        this.bedNo = bedNo;
    }
}