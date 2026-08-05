package com.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.dto.PatientCreateRequest;
import com.example.dto.PatientUpdateRequest;
import com.example.entity.Patient;

import java.util.List;


public interface PatientService extends IService<Patient> {

    Long create(PatientCreateRequest request);

    List<Patient> listPatients();

    Patient getDetail(Long id);

    void update(Long id, PatientUpdateRequest request);

    void delete(Long id);

}
