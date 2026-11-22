-- 已建库时补充美容师入职日期，用于展示工龄。新库直接执行 schema.sql。
ALTER TABLE md_beautician
  ADD COLUMN entry_date DATE NULL COMMENT '入职日期，用于计算工龄' AFTER avatar;
