ALTER TABLE inv_product
  ADD COLUMN max_quantity INT NOT NULL DEFAULT 0 COMMENT '最大数量' AFTER unit;

UPDATE inv_product
SET max_quantity = GREATEST(IFNULL(warn_quantity, 0) * 5, 1);

UPDATE inv_product
SET warn_quantity = FLOOR(max_quantity * 0.2);
