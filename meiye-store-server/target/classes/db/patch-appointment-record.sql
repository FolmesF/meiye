USE meiye_store;

ALTER TABLE biz_appointment
  ADD COLUMN appoint_end DATETIME NULL COMMENT '预约结束时间' AFTER appoint_time,
  ADD COLUMN booking_id BIGINT NULL COMMENT '预约端预约单ID' AFTER appoint_end,
  ADD COLUMN customer_name VARCHAR(64) NULL COMMENT '客户名称' AFTER booking_id,
  ADD COLUMN customer_mobile VARCHAR(20) NULL COMMENT '客户手机号' AFTER customer_name,
  ADD COLUMN service_name VARCHAR(64) NULL COMMENT '项目名称' AFTER customer_mobile,
  ADD COLUMN beautician_name VARCHAR(64) NULL COMMENT '美容师姓名' AFTER service_name,
  ADD UNIQUE KEY uk_appointment_booking (booking_id);

UPDATE biz_appointment a
JOIN biz_customer c ON c.id = a.customer_id
JOIN biz_service_item s ON s.id = a.service_item_id
JOIN biz_beautician b ON b.id = a.beautician_id
SET a.customer_name = c.name,
    a.customer_mobile = c.mobile,
    a.service_name = s.name,
    a.beautician_name = b.name,
    a.appoint_end = DATE_ADD(a.appoint_time, INTERVAL s.duration_min MINUTE)
WHERE a.customer_name IS NULL;
