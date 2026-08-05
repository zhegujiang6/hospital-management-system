package com.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.dto.VisitRecordCreateRequest;
import com.example.entity.VisitRecord;
import com.example.vo.RegistrationOrderVO;
import com.example.vo.VisitRecordVO;

import java.util.List;

public interface VisitRecordService
        extends IService<VisitRecord> {

    Long create(
            Long currentDoctorId,
            VisitRecordCreateRequest request
    );

    List<VisitRecordVO> listByDoctorId(
            Long currentDoctorId
    );

    VisitRecordVO getDetail(
            Long id,
            Long currentDoctorId
    );

    List<RegistrationOrderVO> listPendingVisits(
            Long currentDoctorId
    );
    List<VisitRecordVO> listAll();

    VisitRecordVO getAdminDetail(Long id);
}