-- 允许患者账号与患者档案建立一对一关系。
-- 唯一索引允许多个 NULL，但同一个患者不能绑定多个账号。
ALTER TABLE sys_user
    ADD COLUMN patient_id BIGINT NULL COMMENT '绑定的患者ID' AFTER doctor_id,
    ADD UNIQUE INDEX uk_sys_user_patient_id (patient_id),
    ADD CONSTRAINT fk_sys_user_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT;


-- 病区，例如：心内科一病区、外科二病区。
CREATE TABLE ward
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '病区ID',
    ward_no       VARCHAR(30)  NOT NULL COMMENT '病区编号',
    department_id BIGINT       NOT NULL COMMENT '所属科室ID',
    name          VARCHAR(100) NOT NULL COMMENT '病区名称',
    building      VARCHAR(100) NOT NULL COMMENT '所在楼栋',
    floor_no      VARCHAR(20)  NOT NULL COMMENT '所在楼层，例如3F、B1',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_ward_no (ward_no),
    INDEX idx_ward_department (department_id),

    CONSTRAINT fk_ward_department
        FOREIGN KEY (department_id)
            REFERENCES department (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '医院病区表';


-- 床位属于某个病区，房间号与床位号共同确定具体位置。
CREATE TABLE bed
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '床位ID',
    ward_id     BIGINT      NOT NULL COMMENT '所属病区ID',
    room_no     VARCHAR(30) NOT NULL COMMENT '房间号',
    bed_no      VARCHAR(30) NOT NULL COMMENT '床位号',
    status      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        COMMENT 'AVAILABLE空闲，OCCUPIED占用，MAINTENANCE维护',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_bed_location (ward_id, room_no, bed_no),
    INDEX idx_bed_ward_status (ward_id, status),

    CONSTRAINT fk_bed_ward
        FOREIGN KEY (ward_id)
            REFERENCES ward (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_bed_status
        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '病区床位表';


-- 一次入院到出院是一条住院记录。
CREATE TABLE inpatient_admission
(
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '住院记录ID',
    admission_no      VARCHAR(40)  NOT NULL COMMENT '住院号',
    patient_id        BIGINT       NOT NULL COMMENT '患者ID',
    bed_id            BIGINT       NOT NULL COMMENT '当前床位ID',
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE住院中，DISCHARGED已出院',
    dietary_notes     VARCHAR(500) NULL COMMENT '饮食注意事项',
    admitted_at       DATETIME     NOT NULL COMMENT '入院时间',
    discharged_at     DATETIME     NULL COMMENT '出院时间',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    -- 住院状态下生成真实ID；出院后生成NULL。
    -- MySQL唯一索引允许多个NULL，因此可以保留历史住院记录。
    active_patient_id BIGINT GENERATED ALWAYS AS
        (CASE WHEN status = 'ACTIVE' THEN patient_id ELSE NULL END) STORED,

    active_bed_id     BIGINT GENERATED ALWAYS AS
        (CASE WHEN status = 'ACTIVE' THEN bed_id ELSE NULL END) STORED,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_admission_no (admission_no),
    UNIQUE INDEX uk_active_patient (active_patient_id),
    UNIQUE INDEX uk_active_bed (active_bed_id),
    INDEX idx_admission_patient_time (patient_id, admitted_at),
    INDEX idx_admission_status (status),

    CONSTRAINT fk_admission_patient
        FOREIGN KEY (patient_id)
            REFERENCES patient (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_admission_bed
        FOREIGN KEY (bed_id)
            REFERENCES bed (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_admission_status
        CHECK (status IN ('ACTIVE', 'DISCHARGED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '患者住院记录表';