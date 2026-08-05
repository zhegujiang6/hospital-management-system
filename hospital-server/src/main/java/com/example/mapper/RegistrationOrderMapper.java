package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.RegistrationOrder;
import com.example.vo.RegistrationOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RegistrationOrderMapper
        extends BaseMapper<RegistrationOrder> {

    @Select("""
            SELECT
                ro.id,
                ro.order_no,
                ro.request_id,
                ro.patient_id,
                p.patient_no,
                p.name AS patient_name,
                ro.schedule_id,
                ds.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.department_id,
                dep.name AS department_name,
                ds.schedule_date,
                ds.period,
                ro.amount,
                ro.status,
                ro.payment_deadline,
                ro.paid_time,
                ro.cancel_time,
                ro.cancel_reason,
                ro.create_time,
                ro.update_time
            FROM registration_order ro
            INNER JOIN patient p
                ON p.id = ro.patient_id
            INNER JOIN doctor_schedule ds
                ON ds.id = ro.schedule_id
            INNER JOIN doctor d
                ON d.id = ds.doctor_id
            INNER JOIN department dep
                ON dep.id = d.department_id
            ORDER BY ro.create_time DESC
            """)
    List<RegistrationOrderVO> selectOrderList();

    @Select("""
            SELECT
                ro.id,
                ro.order_no,
                ro.request_id,
                ro.patient_id,
                p.patient_no,
                p.name AS patient_name,
                ro.schedule_id,
                ds.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.department_id,
                dep.name AS department_name,
                ds.schedule_date,
                ds.period,
                ro.amount,
                ro.status,
                ro.payment_deadline,
                ro.paid_time,
                ro.cancel_time,
                ro.cancel_reason,
                ro.create_time,
                ro.update_time
            FROM registration_order ro
            INNER JOIN patient p
                ON p.id = ro.patient_id
            INNER JOIN doctor_schedule ds
                ON ds.id = ro.schedule_id
            INNER JOIN doctor d
                ON d.id = ds.doctor_id
            INNER JOIN department dep
                ON dep.id = d.department_id
            WHERE ro.id = #{id}
            """)
    RegistrationOrderVO selectOrderDetail(
            @Param("id") Long id
    );
    @Select("""
        SELECT id
        FROM registration_order
        WHERE status = 'PENDING_PAYMENT'
          AND payment_deadline < NOW()
        ORDER BY payment_deadline
        LIMIT #{limit}
        """)
    List<Long> selectExpiredPendingIds(
            @Param("limit") int limit
    );

    @Update("""
        UPDATE registration_order
        SET status = 'EXPIRED',
            cancel_time = NOW(),
            cancel_reason = '支付超时自动关闭'
        WHERE id = #{id}
          AND status = 'PENDING_PAYMENT'
          AND payment_deadline < NOW()
        """)
    int markExpired(@Param("id") Long id);

    @Select("""
        SELECT
            ro.id,
            ro.order_no,
            ro.request_id,

            ro.patient_id,
            p.patient_no,
            p.name AS patient_name,

            ro.schedule_id,

            ds.doctor_id,
            d.doctor_no,
            d.name AS doctor_name,

            d.department_id,
            dep.name AS department_name,

            ds.schedule_date,
            ds.period,

            ro.amount,
            ro.status,
            ro.payment_deadline,
            ro.paid_time,
            ro.cancel_time,
            ro.cancel_reason,
            ro.create_time,
            ro.update_time

        FROM registration_order ro

        INNER JOIN patient p
            ON p.id = ro.patient_id

        INNER JOIN doctor_schedule ds
            ON ds.id = ro.schedule_id

        INNER JOIN doctor d
            ON d.id = ds.doctor_id

        INNER JOIN department dep
            ON dep.id = d.department_id

        WHERE ds.doctor_id = #{doctorId}

          AND ro.status = 'PAID'

          AND ds.schedule_date <= CURDATE()

          AND NOT EXISTS (
              SELECT 1
              FROM visit_record vr
              WHERE vr.registration_order_id = ro.id
          )

        ORDER BY
            ds.schedule_date ASC,
            ro.paid_time ASC
        """)
    List<RegistrationOrderVO>
    selectPendingVisitListByDoctorId(
            @Param("doctorId") Long doctorId
    );


}