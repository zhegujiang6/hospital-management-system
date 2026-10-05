package com.example.payment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentOrderMapper
        extends BaseMapper<PaymentOrder> {

    @Select("""
            SELECT *
            FROM payment_order
            WHERE id = #{id}
            FOR UPDATE
            """)
    PaymentOrder selectByIdForUpdate(@Param("id") Long id);

}