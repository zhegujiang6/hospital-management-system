package com.example.visitrecord;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisitRecordMapper
        extends BaseMapper<VisitRecord> {

    @Select("""
            SELECT
                vr.id,
                vr.record_no,
                vr.registration_order_id,

                ro.order_no,
                ro.status AS registration_status,

                vr.patient_id,
                p.patient_no,
                p.name AS patient_name,
                p.gender AS patient_gender,
                p.birth_date AS patient_birth_date,
                p.phone AS patient_phone,

                vr.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.title AS doctor_title,

                d.department_id,
                dep.name AS department_name,

                ds.schedule_date,
                ds.period,

                vr.chief_complaint,
                vr.present_illness,
                vr.diagnosis,
                vr.treatment_plan,
                vr.doctor_advice,
                vr.visit_time,
                vr.create_time,
                vr.update_time

            FROM visit_record vr

            INNER JOIN registration_order ro
                ON ro.id = vr.registration_order_id

            INNER JOIN patient p
                ON p.id = vr.patient_id

            INNER JOIN doctor d
                ON d.id = vr.doctor_id

            INNER JOIN department dep
                ON dep.id = d.department_id

            INNER JOIN doctor_schedule ds
                ON ds.id = ro.schedule_id

            WHERE (
                #{doctorId} IS NULL
                OR vr.doctor_id = #{doctorId}
            )

            ORDER BY vr.visit_time DESC
            """)
    List<VisitRecordVO> selectRecordList(
            @Param("doctorId") Long doctorId
    );

    @Select("""
            SELECT
                vr.id,
                vr.record_no,
                vr.registration_order_id,

                ro.order_no,
                ro.status AS registration_status,

                vr.patient_id,
                p.patient_no,
                p.name AS patient_name,
                p.gender AS patient_gender,
                p.birth_date AS patient_birth_date,
                p.phone AS patient_phone,

                vr.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.title AS doctor_title,

                d.department_id,
                dep.name AS department_name,

                ds.schedule_date,
                ds.period,

                vr.chief_complaint,
                vr.present_illness,
                vr.diagnosis,
                vr.treatment_plan,
                vr.doctor_advice,
                vr.visit_time,
                vr.create_time,
                vr.update_time

            FROM visit_record vr

            INNER JOIN registration_order ro
                ON ro.id = vr.registration_order_id

            INNER JOIN patient p
                ON p.id = vr.patient_id

            INNER JOIN doctor d
                ON d.id = vr.doctor_id

            INNER JOIN department dep
                ON dep.id = d.department_id

            INNER JOIN doctor_schedule ds
                ON ds.id = ro.schedule_id

            WHERE vr.id = #{id}
              AND (
                  #{doctorId} IS NULL
                  OR vr.doctor_id = #{doctorId}
              )
            """)
    VisitRecordVO selectRecordDetail(
            @Param("id") Long id,
            @Param("doctorId") Long doctorId
    );
}