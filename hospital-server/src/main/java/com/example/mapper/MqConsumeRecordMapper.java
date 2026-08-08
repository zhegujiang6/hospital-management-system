package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MqConsumeRecordMapper {

    /**
     * 尝试记录一条消息的消费结果。
     * INSERT IGNORE配合唯一索引：第一次插入返回1，重复消息返回0。
     */
    @Insert("""
            INSERT IGNORE INTO mq_consume_record (
                event_id,
                consumer_name
            ) VALUES (
                #{eventId},
                #{consumerName}
            )
            """)
    int insertIfAbsent(
            @Param("eventId") String eventId,
            @Param("consumerName") String consumerName
    );
}
