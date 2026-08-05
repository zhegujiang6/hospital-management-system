package com.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.dto.DoctorCreateRequest;
import com.example.dto.DoctorUpdateRequest;
import com.example.entity.Doctor;
import com.example.vo.DoctorVO;
import jakarta.validation.Valid;

import java.util.List;

public interface DoctorService extends IService<Doctor> {


    Long create(DoctorCreateRequest request);

    List<DoctorVO> listDetails();

    DoctorVO getDetail(Long id);
    void delete(Long id);
    void update(Long id, DoctorUpdateRequest request);
}
