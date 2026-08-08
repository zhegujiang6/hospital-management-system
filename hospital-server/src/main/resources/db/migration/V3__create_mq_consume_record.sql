CREATE TABLE `mq_consume_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消费记录ID',
  `event_id` varchar(64) NOT NULL COMMENT '消息唯一事件ID',
  `consumer_name` varchar(100) NOT NULL COMMENT '消费者名称',
  `consume_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费成功时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mq_event_consumer` (`event_id`, `consumer_name`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'RabbitMQ消息消费记录表';
