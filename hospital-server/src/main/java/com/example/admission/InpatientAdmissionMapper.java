package com.example.admission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InpatientAdmissionMapper
        extends BaseMapper<InpatientAdmission> {

    /**
     * 查询全部住院记录及其患者和床位信息。
     */
    @Select("""
            SELECT
                ia.id,
                ia.admission_no AS admissionNo,

                ia.patient_id AS patientId,
                p.patient_no AS patientNo,
                p.name AS patientName,
                p.phone AS patientPhone,

                ia.bed_id AS bedId,
                w.id AS wardId,
                w.ward_no AS wardNo,
                w.name AS wardName,
                d.name AS departmentName,

                w.building,
                w.floor_no AS floorNo,
                b.room_no AS roomNo,
                b.bed_no AS bedNo,

                ia.status,
                ia.dietary_notes AS dietaryNotes,
                ia.admitted_at AS admittedAt,
                ia.discharged_at AS dischargedAt,
                ia.create_time AS createTime,
                ia.update_time AS updateTime
            FROM inpatient_admission ia
            INNER JOIN patient p
                ON ia.patient_id = p.id
            INNER JOIN bed b
                ON ia.bed_id = b.id
            INNER JOIN ward w
                ON b.ward_id = w.id
            INNER JOIN department d
                ON w.department_id = d.id
            ORDER BY ia.admitted_at DESC
            """)
    List<InpatientAdmissionVO> selectInpatientAdmissionList();

    /**
     * 根据住院记录ID查询完整住院详情。
     */
    @Select("""
            SELECT
                ia.id,
                ia.admission_no AS admissionNo,

                ia.patient_id AS patientId,
                p.patient_no AS patientNo,
                p.name AS patientName,
                p.phone AS patientPhone,

                ia.bed_id AS bedId,
                w.id AS wardId,
                w.ward_no AS wardNo,
                w.name AS wardName,
                d.name AS departmentName,

                w.building,
                w.floor_no AS floorNo,
                b.room_no AS roomNo,
                b.bed_no AS bedNo,

                ia.status,
                ia.dietary_notes AS dietaryNotes,
                ia.admitted_at AS admittedAt,
                ia.discharged_at AS dischargedAt,
                ia.create_time AS createTime,
                ia.update_time AS updateTime
            FROM inpatient_admission ia
            INNER JOIN patient p
                ON ia.patient_id = p.id
            INNER JOIN bed b
                ON ia.bed_id = b.id
            INNER JOIN ward w
                ON b.ward_id = w.id
            INNER JOIN department d
                ON w.department_id = d.id
            WHERE ia.id = #{id}
            """)
    InpatientAdmissionVO selectInpatientAdmissionDetail(
            @Param("id") Long id
    );
}
