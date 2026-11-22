-- 已建库时补充账户资料、用户标签和门店评分。新库直接执行 schema.sql。
USE meiye_booking;

ALTER TABLE c_user
  ADD COLUMN address VARCHAR(255) NULL COMMENT '联系地址' AFTER birthday,
  ADD COLUMN member_level VARCHAR(32) NOT NULL DEFAULT '普通会员' COMMENT '会员等级，仅店管可设置' AFTER address;

CREATE TABLE IF NOT EXISTS c_user_tag (
  id          BIGINT       NOT NULL COMMENT '主键',
  user_id     BIGINT       NOT NULL COMMENT '用户ID',
  tag_name    VARCHAR(16)  NOT NULL COMMENT '标签名',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_tag (user_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

ALTER TABLE bk_review
  ADD COLUMN store_score TINYINT NOT NULL DEFAULT 5 COMMENT '门店评分 1-5' AFTER beautician_score;
