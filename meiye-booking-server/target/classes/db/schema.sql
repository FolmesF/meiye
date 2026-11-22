-- 掌美预约 meiye_booking
-- 门店、项目、美容师、排班是店管主数据的只读快照，主键与店管保持一致
-- 预约、消费、评价、站内消息由本服务写入

CREATE DATABASE IF NOT EXISTS meiye_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE meiye_booking;

CREATE TABLE c_user (
  id          BIGINT       NOT NULL COMMENT '主键',
  mobile      VARCHAR(20)  NOT NULL COMMENT '手机号，登录账号',
  nickname    VARCHAR(64)  NULL COMMENT '昵称',
  avatar      VARCHAR(255) NULL COMMENT '头像地址',
  gender       VARCHAR(16)  NULL COMMENT '性别 MALE/FEMALE/UNKNOWN',
  birthday     DATE         NULL COMMENT '出生日期',
  address      VARCHAR(255) NULL COMMENT '联系地址',
  member_level VARCHAR(32)  NOT NULL DEFAULT '普通会员' COMMENT '会员等级，仅店管可设置',
  status       VARCHAR(16)  NOT NULL COMMENT 'ACTIVE/DISABLED',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户';

CREATE TABLE c_user_tag (
  id          BIGINT       NOT NULL COMMENT '主键',
  user_id     BIGINT       NOT NULL COMMENT '用户ID',
  tag_name    VARCHAR(16)  NOT NULL COMMENT '标签名',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_tag (user_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

CREATE TABLE md_store (
  id          BIGINT       NOT NULL COMMENT '门店ID，与店管一致',
  name        VARCHAR(64)  NOT NULL COMMENT '门店名称',
  phone       VARCHAR(20)  NULL COMMENT '联系电话',
  address     VARCHAR(255) NULL COMMENT '地址',
  status      VARCHAR(16)  NOT NULL COMMENT 'ENABLED/DISABLED',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店快照';

CREATE TABLE md_service_item (
  id            BIGINT         NOT NULL COMMENT '项目ID，与店管一致',
  store_id      BIGINT         NOT NULL COMMENT '门店ID',
  name          VARCHAR(64)    NOT NULL COMMENT '项目名称',
  price         DECIMAL(12,2)  NOT NULL COMMENT '标价，作为消费金额',
  duration_min  INT            NOT NULL COMMENT '服务时长（分钟）',
  status        VARCHAR(16)    NOT NULL COMMENT 'ENABLED/DISABLED',
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_service_store_status (store_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目快照';

CREATE TABLE md_beautician (
  id          BIGINT       NOT NULL COMMENT '美容师ID，与店管一致',
  store_id    BIGINT       NOT NULL COMMENT '门店ID',
  name        VARCHAR(64)  NOT NULL COMMENT '姓名',
  level       VARCHAR(32)  NULL COMMENT '职级',
  avatar      VARCHAR(255) NULL COMMENT '头像',
  entry_date  DATE         NULL COMMENT '入职日期，用于计算工龄',
  status      VARCHAR(16)  NOT NULL COMMENT 'ACTIVE/LEFT',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_beautician_store_status (store_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师快照';

CREATE TABLE md_beautician_service (
  id               BIGINT   NOT NULL COMMENT '主键',
  beautician_id    BIGINT   NOT NULL COMMENT '美容师ID',
  service_item_id  BIGINT   NOT NULL COMMENT '项目ID',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_beautician_service (beautician_id, service_item_id),
  KEY idx_bind_service (service_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师可做项目';

CREATE TABLE md_schedule (
  id             BIGINT      NOT NULL COMMENT '排班ID，与店管一致',
  store_id       BIGINT      NOT NULL COMMENT '门店ID',
  beautician_id  BIGINT      NOT NULL COMMENT '美容师ID',
  work_date      DATE        NOT NULL COMMENT '日期',
  shift_status   VARCHAR(16) NOT NULL COMMENT 'ON_DUTY/OFF/LEAVE',
  shift_start    TIME        NULL COMMENT '上班时间',
  shift_end      TIME        NULL COMMENT '下班时间',
  created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_beautician_date (beautician_id, work_date),
  KEY idx_schedule_store_date (store_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容师排班快照';

CREATE TABLE bk_appointment (
  id               BIGINT         NOT NULL COMMENT '主键',
  user_id          BIGINT         NOT NULL COMMENT '用户ID',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  beautician_id    BIGINT         NOT NULL COMMENT '美容师ID',
  service_item_id  BIGINT         NOT NULL COMMENT '项目ID',
  appoint_time     DATETIME       NOT NULL COMMENT '开始时间',
  appoint_end      DATETIME       NOT NULL COMMENT '结束时间',
  status           VARCHAR(16)    NOT NULL COMMENT 'PENDING/CONFIRMED/ARRIVED/COMPLETED/CANCELLED',
  amount           DECIMAL(12,2)  NOT NULL COMMENT '预约金额',
  slot_lock        VARCHAR(64)    NULL COMMENT '占用键：美容师ID:yyyyMMddHHmm，取消后置空',
  cancel_reason    VARCHAR(255)   NULL COMMENT '取消原因',
  reminded         TINYINT        NOT NULL DEFAULT 0 COMMENT '是否已发到店提醒 0否 1是',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted          TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_slot_lock (slot_lock),
  KEY idx_appointment_user_time (user_id, appoint_time),
  KEY idx_appointment_beautician_time (beautician_id, appoint_time, status),
  KEY idx_appointment_remind (status, reminded, appoint_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线预约';

CREATE TABLE bk_consumption (
  id               BIGINT         NOT NULL COMMENT '主键',
  user_id          BIGINT         NOT NULL COMMENT '用户ID',
  store_id         BIGINT         NOT NULL COMMENT '门店ID',
  appointment_id   BIGINT         NOT NULL COMMENT '预约ID',
  beautician_id    BIGINT         NOT NULL COMMENT '美容师ID',
  service_item_id  BIGINT         NOT NULL COMMENT '项目ID',
  amount           DECIMAL(12,2)  NOT NULL COMMENT '消费金额',
  consumed_at      DATETIME       NOT NULL COMMENT '消费时间',
  created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_consumption_appointment (appointment_id),
  KEY idx_consumption_user_time (user_id, consumed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消费记录';

CREATE TABLE bk_review (
  id                 BIGINT       NOT NULL COMMENT '主键',
  user_id            BIGINT       NOT NULL COMMENT '用户ID',
  appointment_id     BIGINT       NOT NULL COMMENT '预约ID',
  store_id           BIGINT       NOT NULL COMMENT '门店ID',
  beautician_id      BIGINT       NOT NULL COMMENT '美容师ID',
  service_item_id    BIGINT       NOT NULL COMMENT '项目ID',
  beautician_score   TINYINT      NOT NULL COMMENT '美容师评分 1-5',
  store_score        TINYINT      NOT NULL COMMENT '门店评分 1-5',
  service_score      TINYINT      NOT NULL COMMENT '项目评分 1-5',
  content            VARCHAR(500) NULL COMMENT '文字评价',
  created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_appointment (appointment_id),
  KEY idx_review_beautician (beautician_id, created_at),
  KEY idx_review_service (service_item_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后评价';

CREATE TABLE bk_message (
  id          BIGINT       NOT NULL COMMENT '主键',
  user_id     BIGINT       NOT NULL COMMENT '用户ID',
  msg_type    VARCHAR(32)  NOT NULL COMMENT 'BOOKING_SUCCESS/BOOKING_CANCEL/BOOKING_RESCHEDULE/ARRIVAL_REMIND/CAMPAIGN',
  title       VARCHAR(64)  NOT NULL COMMENT '标题',
  content     VARCHAR(500) NOT NULL COMMENT '内容',
  biz_id      BIGINT       NULL COMMENT '关联预约或营销活动ID',
  read_flag   TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  channel     VARCHAR(16)  NOT NULL DEFAULT 'IN_APP' COMMENT '站内消息固定 IN_APP',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_message_user (user_id, id),
  KEY idx_message_biz (user_id, msg_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';
