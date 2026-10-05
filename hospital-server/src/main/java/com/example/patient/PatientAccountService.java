package com.example.patient;


public interface PatientAccountService {

    /**
     * 为现有患者档案创建登录账号。
     */
    Long create(PatientAccountCreateRequest request);
}