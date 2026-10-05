-- 餐厅档口表，一个医院可以有多个餐厅或档口
CREATE TABLE meal_store
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '餐厅ID',
    store_no    VARCHAR(30)  NOT NULL COMMENT '餐厅编号',
    name        VARCHAR(100) NOT NULL COMMENT '餐厅名称',
    location    VARCHAR(200) NOT NULL COMMENT '餐厅位置',
    phone       VARCHAR(30)  NULL COMMENT '联系电话',
    status      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED'
        COMMENT 'ENABLED启用，DISABLED停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_meal_store_no (store_no),

    CONSTRAINT ck_meal_store_status
        CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '餐厅档口表';


-- 菜品分类表，例如主食、粥类、低糖套餐
CREATE TABLE meal_category
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    store_id    BIGINT       NOT NULL COMMENT '所属餐厅ID',
    name        VARCHAR(100) NOT NULL COMMENT '分类名称',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED'
        COMMENT 'ENABLED启用，DISABLED停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_meal_category_name (store_id, name),
    INDEX idx_meal_category_store_status (store_id, status),

    CONSTRAINT fk_meal_category_store
        FOREIGN KEY (store_id)
            REFERENCES meal_store (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_meal_category_status
        CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '菜品分类表';


-- 菜品表，保存菜品本身相对固定的信息
CREATE TABLE meal_product
(
    id            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
    category_id   BIGINT         NOT NULL COMMENT '所属分类ID',
    product_no    VARCHAR(40)    NOT NULL COMMENT '菜品编号',
    name          VARCHAR(100)   NOT NULL COMMENT '菜品名称',
    description   VARCHAR(1000)  NULL COMMENT '菜品描述',
    price         DECIMAL(10, 2) NOT NULL COMMENT '菜品单价',
    image_url     VARCHAR(500)   NULL COMMENT '菜品图片地址',
    dietary_tags  VARCHAR(255)   NULL COMMENT '饮食标签，例如低盐、低糖',
    allergen_info VARCHAR(500)   NULL COMMENT '过敏原信息',
    status        VARCHAR(20)    NOT NULL DEFAULT 'ON_SALE'
        COMMENT 'ON_SALE上架，OFF_SALE下架',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_meal_product_no (product_no),
    INDEX idx_meal_product_category_status (category_id, status),

    CONSTRAINT fk_meal_product_category
        FOREIGN KEY (category_id)
            REFERENCES meal_category (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_meal_product_price
        CHECK (price >= 0),

    CONSTRAINT ck_meal_product_status
        CHECK (status IN ('ON_SALE', 'OFF_SALE'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '菜品表';


-- 每日库存表，同一菜品在不同日期、餐次拥有不同库存
CREATE TABLE meal_product_stock
(
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '库存ID',
    product_id      BIGINT      NOT NULL COMMENT '菜品ID',
    service_date    DATE        NOT NULL COMMENT '供应日期',
    meal_period     VARCHAR(20) NOT NULL
        COMMENT 'BREAKFAST早餐，LUNCH午餐，DINNER晚餐',
    total_stock     INT         NOT NULL COMMENT '总库存',
    available_stock INT         NOT NULL COMMENT '剩余可用库存',
    version         INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE INDEX uk_meal_product_stock
        (product_id, service_date, meal_period),
    INDEX idx_meal_stock_date_period
        (service_date, meal_period),

    CONSTRAINT fk_meal_stock_product
        FOREIGN KEY (product_id)
            REFERENCES meal_product (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT ck_meal_stock_period
        CHECK (meal_period IN ('BREAKFAST', 'LUNCH', 'DINNER')),

    CONSTRAINT ck_meal_total_stock
        CHECK (total_stock >= 0),

    CONSTRAINT ck_meal_available_stock
        CHECK (available_stock >= 0 AND available_stock <= total_stock)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '菜品每日库存表';