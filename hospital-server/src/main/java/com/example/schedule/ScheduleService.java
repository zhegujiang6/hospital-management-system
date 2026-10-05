package com.example.schedule;


import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface ScheduleService extends IService<DoctorSchedule>{

    Long create(ScheduleCreateRequest request);

    List<ScheduleVO> listDetails();

    ScheduleVO getDetail(Long id);

    void update(Long id, ScheduleUpdateRequest request);

    void delete(Long id);

    boolean decreaseSlot(Long scheduleId);

    void restoreSlot(Long scheduleId);

    List<ScheduleVO> listByDoctorId(
            Long currentDoctorId
    );

}
