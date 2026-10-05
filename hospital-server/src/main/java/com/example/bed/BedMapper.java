package com.example.bed;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    @Select("""
            SELECT
                b.id,
                b.ward_id AS wardId,
                w.ward_no AS wardNo,
                w.name AS wardName,
                d.name AS departmentName,
                b.room_no AS roomNo,
                b.bed_no AS bedNo,
                b.status,
                b.create_time AS createTime,
                b.update_time AS updateTime
            FROM bed b
            INNER JOIN ward w
                ON b.ward_id = w.id
            INNER JOIN department d
                ON w.department_id = d.id
            ORDER BY
                w.id,
                b.room_no,
                b.bed_no
            """)
    List<BedVO> selectBedList();

    @Select("""
            SELECT
                b.id,
                b.ward_id AS wardId,
                w.ward_no AS wardNo,
                w.name AS wardName,
                d.name AS departmentName,
                b.room_no AS roomNo,
                b.bed_no AS bedNo,
                b.status,
                b.create_time AS createTime,
                b.update_time AS updateTime
            FROM bed b
            INNER JOIN ward w
                ON b.ward_id = w.id
            INNER JOIN department d
                ON w.department_id = d.id
            WHERE b.id = #{id}
            """)
    BedVO selectBedDetail(
            @Param("id") Long id
    );
}