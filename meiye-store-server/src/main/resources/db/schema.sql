-- 掌美店管 meiye_store
-- 金额 DECIMAL(12,2)，手工费比例 DECIMAL(8,4)，0.1500 表示 15%

CREATE DATABASE IF NOT EXISTS meiye_store DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE meiye_store;

CREATE TABLE biz_customer (
  id            BIGINT         NOT NULL COMMENT '主键',
  store_id      BIGINT         NOT NULL COMMENT '门店ID',
  name          VARCHAR(64)    NOT NULL COMMENT '姓名',
  mobile        VARCHAR(20)    NOT NULL COMMENT '手机号',
  gender        VARCHAR(16)    NULL COMMENT '性别 MALE/FEMALE/UNKNOWN',
  birthday      DATE           NULL COMMENT '生日',
  level         VARCHAR(32)    NULL COMMENT '会员等级',
  source        VARCHAR(32)    NULL COMMENT '客户来源',
  address       VARCHAR(255)   NULL COMMENT '联系地址',
  remark        VARCHAR(255)   NULL COMMENT '备注',
  status        VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 正常 / DISABLED 禁用',
  total_amount  DECIMAL(12,2)  NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  visit_count   INT            NOT NULL DEFAULT 0 COMMENT '消费次数',
  last_visit_at DATETIME       NULL COMMENT '末次到店时间',
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_customer_store_mobile (store_id, mobile),
  KEY idx_customer_store_name (store_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客档案';

CREATE TABLE biz_customer_tag (
  id           BIGINT       NOT NULL COMMENT '主键',
  store_id     BIGINT       NOT NULL COMMENT '门店ID',
  customer_id  BIGINT       NOT NULL COMMENT '顾客ID',
  tag_name     VARCHAR(32)  NOT NULL COMMENT '标签名',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_tag (customer_id, tag_name),
  KEY idx_tag_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客标签';

CREATE TABLE biz_consumption (
  id               BIGINT         NOT NULL COMMENT '主键',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  customer_id      BIGINT         NOT NULL COMMENT '顾客ID',
  beautician_id    BIGINT         NULL COMMENT '美容师ID',
  service_item_id  BIGINT         NULL COMMENT '服务项目ID',
  appointment_id   BIGINT         NULL COMMENT '预约ID',
  amount           DECIMAL(12,2)  NOT NULL COMMENT '消费金额',
  consumed_at      DATETIME       NOT NULL COMMENT '消费时间',
  remark           VARCHAR(255)   NULL COMMENT '备注',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_consumption_customer_time (customer_id, consumed_at),
  KEY idx_consumption_store_time (store_id, consumed_at),
  KEY idx_consumption_appointment (appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客消费记录';

CREATE TABLE biz_beautician (
  id          BIGINT       NOT NULL COMMENT '主键',
  store_id    BIGINT       NOT NULL COMMENT '门店ID',
  name        VARCHAR(64)  NOT NULL COMMENT '姓名',
  mobile      VARCHAR(20)  NOT NULL COMMENT '手机号',
  gender      VARCHAR(16)  NULL COMMENT '性别 MALE/FEMALE/UNKNOWN',
  level       VARCHAR(32)  NULL COMMENT '职级',
  status      VARCHAR(16)  NOT NULL COMMENT '在职状态 ACTIVE/LEFT',
  entry_date  DATE         NULL COMMENT '入职日期',
  remark      VARCHAR(255) NULL COMMENT '备注',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_beautician_store_status (store_id, status),
  KEY idx_beautician_store_mobile (store_id, mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师档案';

CREATE TABLE biz_beautician_service (
  id               BIGINT   NOT NULL COMMENT '主键',
  store_id         BIGINT   NOT NULL COMMENT '门店ID',
  beautician_id    BIGINT   NOT NULL COMMENT '美容师ID',
  service_item_id  BIGINT   NOT NULL COMMENT '服务项目ID',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_beautician_service (beautician_id, service_item_id),
  KEY idx_beautician_service_item (service_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师可服务项目';

CREATE TABLE biz_beautician_schedule (
  id             BIGINT       NOT NULL COMMENT '主键',
  store_id       BIGINT       NOT NULL COMMENT '门店ID',
  beautician_id  BIGINT       NOT NULL COMMENT '美容师ID',
  work_date      DATE         NOT NULL COMMENT '排班日期',
  shift_status   VARCHAR(16)  NOT NULL COMMENT '排班状态 ON_DUTY/OFF/LEAVE',
  shift_start    TIME         NULL COMMENT '上班时间',
  shift_end      TIME         NULL COMMENT '下班时间',
  remark         VARCHAR(255) NULL COMMENT '备注',
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_beautician_work_date (beautician_id, work_date),
  KEY idx_schedule_store_date (store_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师排班';

CREATE TABLE biz_service_item (
  id            BIGINT         NOT NULL COMMENT '主键',
  store_id      BIGINT         NOT NULL COMMENT '门店ID',
  name          VARCHAR(64)    NOT NULL COMMENT '项目名称',
  category      VARCHAR(64)    NULL COMMENT '项目分类',
  price         DECIMAL(12,2)  NOT NULL COMMENT '标价',
  duration_min  INT            NOT NULL COMMENT '服务时长（分钟）',
  status        VARCHAR(16)    NOT NULL COMMENT '状态 ENABLED/DISABLED',
  remark        VARCHAR(255)   NULL COMMENT '备注',
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_service_store_status (store_id, status),
  KEY idx_service_store_name (store_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目';

CREATE TABLE biz_service_material (
  id               BIGINT         NOT NULL COMMENT '主键',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  service_item_id  BIGINT         NOT NULL COMMENT '服务项目ID',
  product_id       BIGINT         NOT NULL COMMENT '耗材产品ID',
  quantity         DECIMAL(10,2)  NOT NULL COMMENT '单次服务标准用量',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_service_material (service_item_id, product_id),
  KEY idx_service_material_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目耗材配置';

CREATE TABLE biz_appointment (
  id               BIGINT         NOT NULL COMMENT '主键',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  customer_id      BIGINT         NOT NULL COMMENT '顾客ID',
  beautician_id    BIGINT         NOT NULL COMMENT '美容师ID',
  service_item_id  BIGINT         NOT NULL COMMENT '服务项目ID',
  appoint_time     DATETIME       NOT NULL COMMENT '预约到店时间',
  appoint_end      DATETIME       NULL COMMENT '预约结束时间',
  booking_id       BIGINT         NULL COMMENT '预约端预约单ID',
  customer_name    VARCHAR(64)    NULL COMMENT '客户名称',
  customer_mobile  VARCHAR(20)    NULL COMMENT '客户手机号',
  service_name     VARCHAR(64)    NULL COMMENT '项目名称',
  beautician_name  VARCHAR(64)    NULL COMMENT '美容师姓名',
  status           VARCHAR(16)    NOT NULL COMMENT 'PENDING/CONFIRMED/ARRIVED/COMPLETED/CANCELLED/NO_SHOW',
  amount           DECIMAL(12,2)  NOT NULL COMMENT '项目业绩金额',
  cancel_reason    VARCHAR(255)   NULL COMMENT '取消原因',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted          TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_appointment_store_time (store_id, appoint_time),
  KEY idx_appointment_time (appoint_time),
  KEY idx_appointment_beautician_time (beautician_id, appoint_time),
  UNIQUE KEY uk_appointment_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约单';

CREATE TABLE biz_manual_fee_rule (
  id               BIGINT         NOT NULL COMMENT '主键',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  service_item_id  BIGINT         NOT NULL COMMENT '服务项目ID',
  rule_type        VARCHAR(16)    NOT NULL COMMENT 'FIXED 固定金额 / RATE 业绩比例',
  fixed_amount     DECIMAL(12,2)  NULL COMMENT '固定手工费，FIXED 时必填',
  rate             DECIMAL(8,4)   NULL COMMENT '业绩比例，0到1，RATE 时必填',
  effective_from   DATE           NOT NULL COMMENT '生效日期（含）',
  effective_to     DATE           NULL COMMENT '失效日期（含），空表示长期',
  enabled          TINYINT        NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted          TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_fee_rule_item (service_item_id, enabled, effective_from),
  KEY idx_fee_rule_store (store_id, service_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手工费规则';

CREATE TABLE biz_manual_fee_record (
  id               BIGINT         NOT NULL COMMENT '主键',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  beautician_id    BIGINT         NOT NULL COMMENT '美容师ID',
  appointment_id   BIGINT         NOT NULL COMMENT '预约ID',
  service_item_id  BIGINT         NOT NULL COMMENT '服务项目ID',
  rule_id          BIGINT         NOT NULL COMMENT '命中的规则ID',
  rule_type        VARCHAR(16)    NOT NULL COMMENT '核算时的规则类型快照',
  fixed_amount     DECIMAL(12,2)  NULL COMMENT '核算时的固定金额快照',
  rate             DECIMAL(8,4)   NULL COMMENT '核算时的比例快照',
  base_amount      DECIMAL(12,2)  NOT NULL COMMENT '项目业绩基数',
  fee_amount       DECIMAL(12,2)  NOT NULL COMMENT '手工费',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核算时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_fee_appointment (appointment_id),
  KEY idx_fee_beautician_time (beautician_id, created_at),
  KEY idx_fee_store_time (store_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手工费核算记录';

CREATE TABLE inv_product (
  id             BIGINT       NOT NULL COMMENT '主键',
  store_id       BIGINT       NOT NULL COMMENT '门店ID',
  name           VARCHAR(64)  NOT NULL COMMENT '产品名称',
  sku_code       VARCHAR(64)  NOT NULL COMMENT 'SKU 编码',
  unit           VARCHAR(16)  NOT NULL COMMENT '单位',
  max_quantity   INT          NOT NULL DEFAULT 0 COMMENT '最大数量',
  warn_quantity  INT          NOT NULL DEFAULT 0 COMMENT '库存预警，最大数量的 20%',
  status         VARCHAR(16)  NOT NULL COMMENT 'ENABLED/DISABLED',
  remark         VARCHAR(255) NULL COMMENT '备注',
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_product_store_sku (store_id, sku_code),
  KEY idx_product_store_name (store_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存产品';

CREATE TABLE inv_stock (
  id          BIGINT   NOT NULL COMMENT '主键',
  store_id    BIGINT   NOT NULL COMMENT '门店ID',
  product_id  BIGINT   NOT NULL COMMENT '产品ID',
  quantity    INT      NOT NULL DEFAULT 0 COMMENT '现存数量',
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stock_store_product (store_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存现存量';

CREATE TABLE inv_stock_flow (
  id           BIGINT       NOT NULL COMMENT '主键',
  store_id     BIGINT       NOT NULL COMMENT '门店ID',
  product_id   BIGINT       NOT NULL COMMENT '产品ID',
  biz_type     VARCHAR(16)  NOT NULL COMMENT 'IN 入库 / OUT 出库 / STOCKTAKE 盘点',
  change_qty   INT          NOT NULL COMMENT '变动数量，出库为负',
  before_qty   INT          NOT NULL COMMENT '变动前数量',
  after_qty    INT          NOT NULL COMMENT '变动后数量',
  biz_id       BIGINT       NULL COMMENT '关联业务单ID',
  remark       VARCHAR(255) NULL COMMENT '备注',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_flow_product_time (product_id, created_at),
  KEY idx_flow_store_time (store_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水';

CREATE TABLE inv_stocktake (
  id           BIGINT       NOT NULL COMMENT '主键',
  store_id     BIGINT       NOT NULL COMMENT '门店ID',
  product_id   BIGINT       NOT NULL COMMENT '产品ID',
  book_qty     INT          NOT NULL COMMENT '账面数量',
  actual_qty   INT          NOT NULL COMMENT '实盘数量',
  diff_qty     INT          NOT NULL COMMENT '差异，实盘减账面',
  remark       VARCHAR(255) NULL COMMENT '备注',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '盘点时间',
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_stocktake_store_time (store_id, created_at),
  KEY idx_stocktake_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点';

CREATE TABLE biz_mall_goods (
  id          BIGINT         NOT NULL COMMENT '主键',
  store_id    BIGINT         NOT NULL COMMENT '门店ID',
  name        VARCHAR(64)    NOT NULL COMMENT '商品名称',
  category    VARCHAR(32)    NULL COMMENT '分类',
  price       DECIMAL(12,2)  NOT NULL COMMENT '供货价',
  brand       VARCHAR(32)    NULL COMMENT '品牌',
  spec        VARCHAR(64)    NULL COMMENT '规格',
  supplier    VARCHAR(64)    NULL COMMENT '供应商',
  status      VARCHAR(16)    NOT NULL COMMENT '在售/下架',
  created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_mall_store_status (store_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美博城商品';

CREATE TABLE biz_campaign (
  id             BIGINT        NOT NULL COMMENT '主键',
  store_id       BIGINT        NOT NULL COMMENT '门店ID',
  name           VARCHAR(64)   NOT NULL COMMENT '活动名称',
  campaign_type  VARCHAR(32)   NOT NULL COMMENT '活动类型',
  status         VARCHAR(16)   NOT NULL COMMENT '未开始/进行中/已结束',
  start_time     DATETIME      NOT NULL COMMENT '开始时间',
  end_time       DATETIME      NOT NULL COMMENT '结束时间',
  rule_text      VARCHAR(255)  NULL COMMENT '活动规则',
  created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_campaign_store_status (store_id, status),
  KEY idx_campaign_store_time (store_id, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动';
