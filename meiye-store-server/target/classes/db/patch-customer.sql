-- 已建库时补充顾客联系地址和禁用状态。新库直接执行 schema.sql 即可。
ALTER TABLE biz_customer
  ADD COLUMN address VARCHAR(255) NULL COMMENT '联系地址' AFTER source,
  ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 正常 / DISABLED 禁用' AFTER remark;
