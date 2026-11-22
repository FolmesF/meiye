ALTER TABLE meiye_booking.c_user
  ADD COLUMN account_no VARCHAR(32) NULL COMMENT '账户号，按注册手机号生成，与店管顾客相同' AFTER mobile;

UPDATE meiye_booking.c_user
SET account_no = CONCAT('MY', mobile)
WHERE account_no IS NULL OR account_no = '';

ALTER TABLE meiye_booking.c_user
  ADD UNIQUE KEY uk_user_account (account_no);

ALTER TABLE meiye_store.biz_customer
  ADD COLUMN account_no VARCHAR(32) NULL COMMENT '账户号，与预约端用户相同' AFTER mobile;

UPDATE meiye_store.biz_customer c
LEFT JOIN meiye_booking.c_user u ON u.mobile = c.mobile
SET c.account_no = IFNULL(u.account_no, CONCAT('MY', c.mobile))
WHERE c.account_no IS NULL OR c.account_no = '';

ALTER TABLE meiye_store.biz_beautician
  ADD COLUMN session_fee DECIMAL(12,2) NOT NULL DEFAULT 128.00 COMMENT '单次费用' AFTER level;

UPDATE meiye_store.biz_beautician
SET session_fee = 80 + (id % 8) * 20;

ALTER TABLE meiye_store.inv_stock
  ADD COLUMN today_consumed INT NOT NULL DEFAULT 0 COMMENT '今日消耗' AFTER quantity,
  ADD COLUMN total_consumed INT NOT NULL DEFAULT 0 COMMENT '总消耗' AFTER today_consumed,
  ADD COLUMN consume_date DATE NULL COMMENT '今日消耗所属日期' AFTER total_consumed;
