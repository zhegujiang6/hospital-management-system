package com.example.meal.vo;

import com.example.meal.entity.MealStore;

import java.time.LocalDateTime;

/**
 * 返回给前端的餐厅信息。
 */
public class MealStoreVO {

    private Long id;

    private String storeNo;

    private String name;

    private String location;

    private String phone;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 把数据库实体转换成返回给前端的VO。
     *
     * @param mealStore 餐厅数据库实体
     * @return 餐厅VO
     */
    public static MealStoreVO fromEntity(
            MealStore mealStore) {

        MealStoreVO vo = new MealStoreVO();

        vo.setId(mealStore.getId());
        vo.setStoreNo(mealStore.getStoreNo());
        vo.setName(mealStore.getName());
        vo.setLocation(mealStore.getLocation());
        vo.setPhone(mealStore.getPhone());
        vo.setStatus(mealStore.getStatus());
        vo.setCreateTime(mealStore.getCreateTime());
        vo.setUpdateTime(mealStore.getUpdateTime());

        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreNo() {
        return storeNo;
    }

    public void setStoreNo(String storeNo) {
        this.storeNo = storeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}