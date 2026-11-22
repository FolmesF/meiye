ALTER TABLE biz_customer
  ADD COLUMN last_visit_at DATETIME NULL COMMENT '末次到店时间' AFTER visit_count;
