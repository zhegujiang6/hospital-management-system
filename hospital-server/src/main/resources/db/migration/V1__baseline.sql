/*
 Navicat Premium Dump SQL

 Source Server         : campus-server
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : hospital_db

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 06/08/2026 12:45:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department
-- ----------------------------
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科室名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '科室简介',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_department_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '科室表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
CREATE TABLE `doctor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `doctor_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生工号',
  `department_id` bigint NOT NULL COMMENT '所属科室ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  `gender` tinyint NOT NULL COMMENT '性别：1男，2女',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `specialty` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '擅长领域',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生简介',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_no`(`doctor_no` ASC) USING BTREE,
  INDEX `idx_doctor_department_id`(`department_id` ASC) USING BTREE,
  CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2083383709590958083 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor_schedule
-- ----------------------------
CREATE TABLE `doctor_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `schedule_date` date NOT NULL COMMENT '出诊日期',
  `period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '时段：MORNING上午，AFTERNOON下午',
  `registration_fee` decimal(10, 2) NOT NULL COMMENT '挂号费',
  `total_slots` int NOT NULL COMMENT '总号源数',
  `remaining_slots` int NOT NULL COMMENT '剩余号源数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0停诊',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_schedule`(`doctor_id` ASC, `schedule_date` ASC, `period` ASC) USING BTREE,
  CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for patient
-- ----------------------------
CREATE TABLE `patient`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `patient_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '性别',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_patient_no`(`patient_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_patient_id_card`(`id_card` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_order
-- ----------------------------
CREATE TABLE `payment_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付单ID',
  `payment_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统支付单号',
  `registration_order_id` bigint NOT NULL COMMENT '挂号订单ID',
  `channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MOCK' COMMENT '支付渠道：MOCK模拟支付，WECHAT微信，ALIPAY支付宝',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待支付，SUCCESS成功，FAILED失败，CLOSED关闭，REFUNDED已退款',
  `gateway_transaction_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方支付平台交易号',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `callback_time` datetime NULL DEFAULT NULL COMMENT '收到回调时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_payment_registration_order`(`registration_order_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_gateway_transaction_no`(`gateway_transaction_no` ASC) USING BTREE,
  INDEX `idx_payment_status`(`status` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_payment_registration_order` FOREIGN KEY (`registration_order_id`) REFERENCES `registration_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for registration_order
-- ----------------------------
CREATE TABLE `registration_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '挂号订单号',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端幂等请求号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '应付金额',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT 'PENDING_PAYMENT待支付，PAID已支付，CANCELLED已取消，COMPLETED已完成',
  `payment_deadline` datetime NOT NULL COMMENT '支付截止时间',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active_flag` tinyint GENERATED ALWAYS AS ((case when (`status` in (_utf8mb4'PENDING_PAYMENT',_utf8mb4'PAID',_utf8mb4'COMPLETED')) then 1 else NULL end)) STORED NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_registration_order_no`(`order_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_registration_request_id`(`request_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_patient_schedule_active`(`patient_id` ASC, `schedule_id` ASC, `active_flag` ASC) USING BTREE,
  INDEX `idx_registration_schedule_status`(`schedule_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_registration_status_deadline`(`status` ASC, `payment_deadline` ASC) USING BTREE,
  CONSTRAINT `fk_registration_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_registration_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `doctor_schedule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '挂号订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户真实姓名',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：ADMIN管理员，DOCTOR医生',
  `doctor_id` bigint NULL DEFAULT NULL COMMENT '绑定的医生ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for visit_record
-- ----------------------------
CREATE TABLE `visit_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '就诊记录ID',
  `record_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '就诊记录编号',
  `registration_order_id` bigint NOT NULL COMMENT '挂号订单ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '接诊医生ID',
  `chief_complaint` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '患者主诉',
  `present_illness` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '现病史',
  `diagnosis` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '诊断结果',
  `treatment_plan` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '治疗方案',
  `doctor_advice` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生嘱咐',
  `visit_time` datetime NOT NULL COMMENT '实际接诊时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_visit_record_no`(`record_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_visit_registration`(`registration_order_id` ASC) USING BTREE,
  INDEX `idx_visit_patient_time`(`patient_id` ASC, `visit_time` ASC) USING BTREE,
  INDEX `idx_visit_doctor_time`(`doctor_id` ASC, `visit_time` ASC) USING BTREE,
  CONSTRAINT `fk_visit_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_visit_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_visit_registration` FOREIGN KEY (`registration_order_id`) REFERENCES `registration_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者就诊记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
