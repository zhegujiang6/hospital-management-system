package com.example.registration;

import com.baomidou.mybatisplus.spring.service.IService;
import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationOrderService
        extends IService<RegistrationOrder> {

    Long create(RegistrationCreateRequest request);

    List<RegistrationOrderVO> listDetails();

    RegistrationOrderVO getDetail(Long id);

    void cancel(
            Long id,
            RegistrationCancelRequest request
    );


    boolean markPaid(Long registrationOrderId, LocalDateTime paidTime);

    List<Long> findExpiredPendingIds(int limit);

    boolean markExpired(Long id);

    boolean markCompleted(Long registrationOrderId);

    List<RegistrationOrderVO>
    listPendingVisitsByDoctorId(Long doctorId);
}
