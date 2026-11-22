-- 掌络 meiye_staff
-- 员工即登录用户。角色、权限、用户角色关联构成 RBAC。

CREATE DATABASE IF NOT EXISTS meiye_staff DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE meiye_staff;

CREATE TABLE hr_employee (
  id              BIGINT         NOT NULL COMMENT '员工ID，即登录用户ID',
  name            VARCHAR(64)    NOT NULL COMMENT '姓名',
  mobile          VARCHAR(20)    NOT NULL COMMENT '手机号',
  job_type        VARCHAR(16)    NOT NULL COMMENT 'GUIDE美导/SALES业务员/ADMIN管理员',
  status          VARCHAR(16)    NOT NULL COMMENT 'ON_DUTY在岗/OFF_DUTY离岗/LEAVE请假/LEFT离职',
  base_longitude  DECIMAL(10,6)  NULL COMMENT '考勤基准经度',
  base_latitude   DECIMAL(10,6)  NULL COMMENT '考勤基准纬度',
  created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_mobile (mobile),
  KEY idx_employee_status (status, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工（用户）';

CREATE TABLE hr_role (
  id          BIGINT       NOT NULL COMMENT '角色ID',
  code        VARCHAR(32)  NOT NULL COMMENT '角色编码',
  name        VARCHAR(32)  NOT NULL COMMENT '角色名称',
  remark      VARCHAR(255) NULL COMMENT '说明',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE hr_permission (
  id          BIGINT       NOT NULL COMMENT '权限ID',
  code        VARCHAR(64)  NOT NULL COMMENT '权限编码',
  name        VARCHAR(64)  NOT NULL COMMENT '权限名称',
  module      VARCHAR(32)  NOT NULL COMMENT '所属模块',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限';

CREATE TABLE hr_role_permission (
  id             BIGINT   NOT NULL COMMENT '主键',
  role_id        BIGINT   NOT NULL COMMENT '角色ID',
  permission_id  BIGINT   NOT NULL COMMENT '权限ID',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permission (role_id, permission_id),
  KEY idx_permission_role (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

CREATE TABLE hr_employee_role (
  id           BIGINT   NOT NULL COMMENT '主键',
  employee_id  BIGINT   NOT NULL COMMENT '用户（员工）ID',
  role_id      BIGINT   NOT NULL COMMENT '角色ID',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_role (employee_id, role_id),
  KEY idx_role_employee (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

CREATE TABLE hr_auth_token (
  token        VARCHAR(64) NOT NULL COMMENT '登录令牌',
  employee_id  BIGINT      NOT NULL COMMENT '员工ID',
  expire_at    DATETIME    NOT NULL COMMENT '过期时间',
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (token),
  KEY idx_token_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录令牌';

CREATE TABLE hr_store (
  id          BIGINT         NOT NULL COMMENT '门店ID',
  name        VARCHAR(64)    NOT NULL COMMENT '门店名称',
  address     VARCHAR(255)   NULL COMMENT '地址',
  phone       VARCHAR(20)    NULL COMMENT '电话',
  longitude   DECIMAL(10,6)  NULL COMMENT '经度',
  latitude    DECIMAL(10,6)  NULL COMMENT '纬度',
  status      VARCHAR(16)    NOT NULL COMMENT 'ENABLED/DISABLED',
  created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_store_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美导负责的门店';

CREATE TABLE hr_employee_store (
  id           BIGINT   NOT NULL COMMENT '主键',
  employee_id  BIGINT   NOT NULL COMMENT '美导员工ID',
  store_id     BIGINT   NOT NULL COMMENT '门店ID',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_store (employee_id, store_id),
  KEY idx_store_employee (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美导与门店关系';

CREATE TABLE hr_inspection (
  id           BIGINT         NOT NULL COMMENT '主键',
  store_id     BIGINT         NOT NULL COMMENT '门店ID',
  employee_id  BIGINT         NOT NULL COMMENT '巡店美导',
  content      VARCHAR(500)   NOT NULL COMMENT '巡店记录',
  longitude    DECIMAL(10,6)  NOT NULL COMMENT '经度',
  latitude     DECIMAL(10,6)  NOT NULL COMMENT '纬度',
  visited_at   DATETIME       NOT NULL COMMENT '巡店时间',
  created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_inspection_store (store_id, visited_at),
  KEY idx_inspection_employee (employee_id, visited_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡店记录';

CREATE TABLE hr_visit_template (
  id           BIGINT       NOT NULL COMMENT '主键',
  employee_id  BIGINT       NOT NULL COMMENT '业务员ID',
  title        VARCHAR(64)  NOT NULL COMMENT '模板标题',
  content      VARCHAR(1000) NOT NULL COMMENT '预设拜访话术',
  required_fields VARCHAR(255) NULL COMMENT '必填项，逗号分隔 mobile,company,address,content,photo,location',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_template_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='陌拜模板';

CREATE TABLE hr_prospect (
  id           BIGINT         NOT NULL COMMENT '主键',
  employee_id  BIGINT         NOT NULL COMMENT '业务员ID',
  template_id  BIGINT         NULL COMMENT '使用的模板ID',
  name         VARCHAR(64)    NOT NULL COMMENT '客户姓名',
  mobile       VARCHAR(20)    NULL COMMENT '手机号',
  company      VARCHAR(128)   NULL COMMENT '单位',
  address      VARCHAR(255)   NULL COMMENT '地址',
  content      VARCHAR(1000)  NULL COMMENT '拜访内容',
  photos       TEXT           NULL COMMENT '现场图片，JSON 数组',
  longitude    DECIMAL(10,6)  NULL COMMENT '经度',
  latitude     DECIMAL(10,6)  NULL COMMENT '纬度',
  status       VARCHAR(16)    NOT NULL COMMENT 'NEW/FOLLOWING/DONE',
  created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (id),
  KEY idx_prospect_employee (employee_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='陌拜客户';

CREATE TABLE hr_followup (
  id             BIGINT       NOT NULL COMMENT '主键',
  prospect_id    BIGINT       NOT NULL COMMENT '陌拜客户ID',
  employee_id    BIGINT       NOT NULL COMMENT '业务员ID',
  content        VARCHAR(500) NOT NULL COMMENT '跟进内容',
  next_follow_at DATETIME     NULL COMMENT '下次跟进时间',
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_followup_prospect (prospect_id, created_at),
  KEY idx_followup_employee (employee_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进记录';

CREATE TABLE hr_attendance (
  id              BIGINT         NOT NULL COMMENT '主键',
  employee_id     BIGINT         NOT NULL COMMENT '员工ID',
  work_date       DATE           NOT NULL COMMENT '考勤日',
  clock_in_at     DATETIME       NOT NULL COMMENT '上班打卡时间',
  clock_in_lng    DECIMAL(10,6)  NOT NULL COMMENT '上班经度',
  clock_in_lat    DECIMAL(10,6)  NOT NULL COMMENT '上班纬度',
  clock_out_at    DATETIME       NULL COMMENT '下班打卡时间',
  clock_out_lng   DECIMAL(10,6)  NULL COMMENT '下班经度',
  clock_out_lat   DECIMAL(10,6)  NULL COMMENT '下班纬度',
  created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_attendance_day (employee_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡';

CREATE TABLE hr_route_report (
  id           BIGINT         NOT NULL COMMENT '主键',
  employee_id  BIGINT         NOT NULL COMMENT '员工ID',
  longitude    DECIMAL(10,6)  NOT NULL COMMENT '经度',
  latitude     DECIMAL(10,6)  NOT NULL COMMENT '纬度',
  address      VARCHAR(255)   NULL COMMENT '位置说明',
  content      VARCHAR(500)   NOT NULL COMMENT '报岗内容',
  report_at    DATETIME       NOT NULL COMMENT '报岗时间',
  created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_route_employee (employee_id, report_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程报岗';

CREATE TABLE hr_performance (
  id             BIGINT         NOT NULL COMMENT '主键',
  employee_id    BIGINT         NOT NULL COMMENT '员工ID',
  store_id       BIGINT         NOT NULL COMMENT '门店ID',
  biz_date       DATE           NOT NULL COMMENT '业务日期',
  amount         DECIMAL(12,2)  NOT NULL COMMENT '业绩金额',
  source_biz_id  VARCHAR(64)    NULL COMMENT '来源单号，重复投递时去重',
  created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_performance_source (source_biz_id),
  KEY idx_performance_employee (employee_id, biz_date),
  KEY idx_performance_store (store_id, biz_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业绩明细';

CREATE TABLE hr_work_note (
  id                  BIGINT       NOT NULL COMMENT '主键',
  target_employee_id  BIGINT       NOT NULL COMMENT '被批注员工',
  author_employee_id  BIGINT       NOT NULL COMMENT '批注管理员',
  biz_type            VARCHAR(32)  NULL COMMENT 'ATTENDANCE/ROUTE/INSPECTION/FOLLOWUP/PERFORMANCE',
  biz_id              BIGINT       NULL COMMENT '关联业务ID',
  content             VARCHAR(500) NOT NULL COMMENT '批注内容',
  created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_note_target (target_employee_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作内容批注';

INSERT INTO hr_role (id, code, name, remark) VALUES
  (1, 'GUIDE', '美导', '门店信息、门店业绩、巡店'),
  (2, 'SALES', '业务员', '陌拜模板、陌拜客户、跟进'),
  (3, 'ADMIN', '管理员', '状态监控、批注、团队业绩');

INSERT INTO hr_permission (id, code, name, module) VALUES
  (1,  'attendance:clock',      '考勤打卡',     'common'),
  (2,  'route:report',          '行程报岗',     'common'),
  (3,  'performance:self',      '个人业绩',     'common'),
  (4,  'store:manage',          '门店信息管理', 'guide'),
  (5,  'performance:store',     '门店业绩',     'guide'),
  (6,  'inspection:report',     '巡店上报',     'guide'),
  (7,  'visit-template:manage', '陌拜模板',     'sales'),
  (8,  'prospect:write',        '陌拜客户',     'sales'),
  (9,  'followup:write',        '客户跟进',     'sales'),
  (10, 'employee:monitor',      '工作状态监控', 'admin'),
  (11, 'work:annotate',         '工作批注',     'admin'),
  (12, 'performance:team',      '团队业绩',     'admin'),
  (13, 'employee:manage',       '员工建档',     'admin');

INSERT INTO hr_role_permission (id, role_id, permission_id) VALUES
  (1,  1, 1), (2,  1, 2), (3,  1, 3), (4,  1, 4), (5,  1, 5), (6,  1, 6),
  (7,  2, 1), (8,  2, 2), (9,  2, 3), (10, 2, 7), (11, 2, 8), (12, 2, 9),
  (13, 3, 1), (14, 3, 2), (15, 3, 3), (16, 3, 10), (17, 3, 11), (18, 3, 12), (19, 3, 13);

INSERT INTO hr_employee (id, name, mobile, job_type, status, base_longitude, base_latitude)
VALUES (1, '系统管理员', '13800000000', 'ADMIN', 'OFF_DUTY', 116.397128, 39.916527);

INSERT INTO hr_employee_role (id, employee_id, role_id) VALUES (1, 1, 3);
