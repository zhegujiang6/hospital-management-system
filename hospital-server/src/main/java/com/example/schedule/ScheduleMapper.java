package com.example.schedule;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ScheduleMapper
        extends BaseMapper<DoctorSchedule> {


    @Select("""
            SELECT
                s.id,
                s.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.department_id,
                dep.name AS department_name,
                s.schedule_date,
                s.period,
                s.registration_fee,
                s.total_slots,
                s.remaining_slots,
                s.status,
                s.create_time,
                s.update_time
            FROM doctor_schedule s
            INNER JOIN doctor d
                ON d.id = s.doctor_id
            INNER JOIN department dep
                ON dep.id = d.department_id
            ORDER BY
                s.schedule_date ASC,
                FIELD(s.period, 'MORNING', 'AFTERNOON')
            """)
    List<ScheduleVO> selectScheduleList();

    @Select("""
            SELECT
                s.id,
                s.doctor_id,
                d.doctor_no,
                d.name AS doctor_name,
                d.department_id,
                dep.name AS department_name,
                s.schedule_date,
                s.period,
                s.registration_fee,
                s.total_slots,
                s.remaining_slots,
                s.status,
                s.create_time,
                s.update_time
            FROM doctor_schedule s
            INNER JOIN doctor d
                ON d.id = s.doctor_id
            INNER JOIN department dep
                ON dep.id = d.department_id
            WHERE s.id = #{id}
            """)
    ScheduleVO selectScheduleDetail(@Param("id") Long id);
    @Update("""
        UPDATE doctor_schedule
        SET remaining_slots = remaining_slots - 1
        WHERE id = #{scheduleId}
          AND status = 1
          AND schedule_date >= CURDATE()
          AND remaining_slots > 0
        """)
    int decreaseRemainingSlot(
            @Param("scheduleId") Long scheduleId
    );

    @Update("""
        UPDATE doctor_schedule
        SET remaining_slots = remaining_slots + 1
        WHERE id = #{scheduleId}
          AND remaining_slots < total_slots
        """)
    int restoreRemainingSlot(
            @Param("scheduleId") Long scheduleId
    );

    @Select("""
        SELECT
            s.id,
            s.doctor_id,

            d.doctor_no,
            d.name AS doctor_name,

            d.department_id,
            dep.name AS department_name,

            s.schedule_date,
            s.period,
            s.registration_fee,
            s.total_slots,
            s.remaining_slots,
            s.status,
            s.create_time,
            s.update_time

        FROM doctor_schedule s

        INNER JOIN doctor d
            ON d.id = s.doctor_id

        INNER JOIN department dep
            ON dep.id = d.department_id

        WHERE s.doctor_id = #{doctorId}

        ORDER BY
            s.schedule_date DESC,
            FIELD(
                s.period,
                'MORNING',
                'AFTERNOON'
            )
        """)
    List<ScheduleVO> selectScheduleListByDoctorId(
            @Param("doctorId") Long doctorId
    );

}
