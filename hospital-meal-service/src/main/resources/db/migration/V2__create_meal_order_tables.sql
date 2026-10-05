-- 餐饮订单主表，保存一整张订单的信息
CREATE TABLE meal_order
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT
        COMMENT '订单ID',

    order_no            VARCHAR(40)    NOT NULL
        COMMENT '订单编号',

    patient_id          BIGINT         NOT NULL
        COMMENT 'hospital-server中的患者ID',

    service_date        DATE           NOT NULL
        COMMENT '供应日期',

    meal_period         VARCHAR(20)    NOT NULL
        COMMENT 'BREAKFAST早餐，LUNCH午餐，DINNER晚餐',

    total_amount        DECIMAL(10, 2) NOT NULL
        COMMENT '订单总金额',

    status              VARCHAR(30)    NOT NULL
                                                DEFAULT 'PENDING_PAYMENT'
        COMMENT '订单状态',

    delivery_type       VARCHAR(30)    NOT NULL
        COMMENT 'WARD病房，DEPARTMENT科室，OTHER其他位置',

    recipient_name      VARCHAR(100)   NOT NULL
        COMMENT '收餐人姓名快照',

    recipient_phone     VARCHAR(30)    NULL
        COMMENT '收餐人联系电话快照',

    delivery_location   VARCHAR(255)   NOT NULL
        COMMENT '完整配送位置，例如内科住院部3楼301病房2床',

    remark              VARCHAR(500)   NULL
        COMMENT '患者备注',

    payment_deadline    DATETIME       NOT NULL
        COMMENT '最晚支付时间',

    paid_time           DATETIME       NULL
        COMMENT '实际支付时间',

    cancel_time         DATETIME       NULL
        COMMENT '订单取消时间',

    version             INT            NOT NULL DEFAULT 0
        COMMENT '订单状态乐观锁版本号',

    create_time         DATETIME       NOT NULL
                                                DEFAULT CURRENT_TIMESTAMP,

    update_time         DATETIME       NOT NULL
                                                DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE INDEX uk_meal_order_no (order_no),

    INDEX idx_meal_order_patient_time
        (patient_id, create_time),

    INDEX idx_meal_order_status_deadline
        (status, payment_deadline),

    CONSTRAINT ck_meal_order_amount
        CHECK (total_amount >= 0),

    CONSTRAINT ck_meal_order_period
        CHECK (
            meal_period IN (
                            'BREAKFAST',
                            'LUNCH',
                            'DINNER'
                )
            ),

    CONSTRAINT ck_meal_order_status
        CHECK (
            status IN (
                       'PENDING_PAYMENT',
                       'PAID',
                       'PREPARING',
                       'DELIVERING',
                       'COMPLETED',
                       'CANCELLED'
                )
            ),

    CONSTRAINT ck_meal_delivery_type
        CHECK (
            delivery_type IN (
                              'WARD',
                              'DEPARTMENT',
                              'OTHER'
                )
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '院内餐饮订单表';





-- 订单明细表，保存订单中的每一种菜品
CREATE TABLE meal_order_item
(
    id              BIGINT         NOT NULL AUTO_INCREMENT
        COMMENT '订单明细ID',

    order_id        BIGINT         NOT NULL
        COMMENT '所属订单ID',

    stock_id        BIGINT         NOT NULL
        COMMENT '下单时对应的分时库存ID',

    product_id      BIGINT         NOT NULL
        COMMENT '菜品ID',

    product_no      VARCHAR(40)    NOT NULL
        COMMENT '下单时的菜品编号快照',

    product_name    VARCHAR(100)   NOT NULL
        COMMENT '下单时的菜品名称快照',

    unit_price      DECIMAL(10, 2) NOT NULL
        COMMENT '下单时的菜品单价快照',

    quantity        INT            NOT NULL
        COMMENT '购买数量',

    subtotal_amount DECIMAL(10, 2) NOT NULL
        COMMENT '本行小计金额',

    create_time     DATETIME       NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE INDEX uk_meal_order_stock
        (order_id, stock_id),

    INDEX idx_meal_order_item_order
        (order_id),

    CONSTRAINT fk_meal_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES meal_order (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_meal_order_item_stock
        FOREIGN KEY (stock_id)
            REFERENCES meal_product_stock (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_meal_order_item_product
        FOREIGN KEY (product_id)
            REFERENCES meal_product (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_meal_order_item_price
        CHECK (unit_price >= 0),

    CONSTRAINT ck_meal_order_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT ck_meal_order_item_subtotal
        CHECK (subtotal_amount >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '院内餐饮订单明细表';