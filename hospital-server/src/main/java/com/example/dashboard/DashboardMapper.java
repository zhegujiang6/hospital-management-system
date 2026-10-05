package com.example.dashboard;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DashboardMapper {

    @Select("""
            SELECT
                (
                    SELECT COUNT(*)
                    FROM department
                    WHERE status = 1
                ) AS enabled_department_count,

                (
                    SELECT COUNT(*)
                    FROM doctor
                    WHERE status = 1
                ) AS enabled_doctor_count,

                (
                    SELECT COUNT(*)
                    FROM patient
                    WHERE status = 1
                ) AS enabled_patient_count,

                (
                    SELECT COUNT(*)
                    FROM doctor_schedule
                    WHERE schedule_date = CURDATE()
                      AND status = 1
                ) AS today_schedule_count,

                (
                    SELECT COUNT(*)
                    FROM registration_order
                    WHERE create_time >= CURDATE()
                      AND create_time < DATE_ADD(
                          CURDATE(),
                          INTERVAL 1 DAY
                      )
                ) AS today_registration_count,

                (
                    SELECT COUNT(*)
                    FROM visit_record
                    WHERE visit_time >= CURDATE()
                      AND visit_time < DATE_ADD(
                          CURDATE(),
                          INTERVAL 1 DAY
                      )
                ) AS today_visit_count,

                (
                    SELECT COUNT(*)
                    FROM registration_order
                    WHERE status = 'PAID'
                ) AS pending_visit_count,

                (
                    SELECT COUNT(*)
                    FROM registration_order
                    WHERE status = 'PENDING_PAYMENT'
                      AND payment_deadline >= NOW()
                ) AS pending_payment_count,

                (
                    SELECT COUNT(*)
                    FROM visit_record
                ) AS total_visit_count,

                (
                    SELECT COALESCE(SUM(amount), 0)
                    FROM payment_order
                    WHERE status = 'SUCCESS'
                      AND paid_time >= CURDATE()
                      AND paid_time < DATE_ADD(
                          CURDATE(),
                          INTERVAL 1 DAY
                      )
                ) AS today_revenue
            """)
    DashboardVO selectOverview();
}