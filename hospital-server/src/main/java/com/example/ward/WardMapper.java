package com.example.ward;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WardMapper extends BaseMapper<Ward> {

    @Select("""
            SELECT
                w.id,
                w.ward_no AS wardNo,
                w.department_id AS departmentId,
                d.name AS departmentName,
                w.name,
                w.building,
                w.floor_no AS floorNo,
                w.status,
                w.create_time AS createTime,
                w.update_time AS updateTime
            FROM ward w
            INNER JOIN department d
                ON w.department_id = d.id
            ORDER BY w.id DESC
            """)
    List<WardVO> selectWardList();

    @Select("""
            SELECT
                w.id,
                w.ward_no AS wardNo,
                w.department_id AS departmentId,
                d.name AS departmentName,
                w.name,
                w.building,
                w.floor_no AS floorNo,
                w.status,
                w.create_time AS createTime,
                w.update_time AS updateTime
            FROM ward w
            INNER JOIN department d
                ON w.department_id = d.id
            WHERE w.id = #{id}
            """)
    WardVO selectWardDetail(
            @Param("id") Long id
    );
}