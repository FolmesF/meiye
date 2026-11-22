-- 已建库时补充陌拜话术必填项、拜访内容和现场图片。新库直接执行 schema.sql。
ALTER TABLE hr_visit_template
  ADD COLUMN required_fields VARCHAR(255) NULL COMMENT '必填项，逗号分隔 mobile,company,address,content,photo,location' AFTER content;

ALTER TABLE hr_prospect
  ADD COLUMN content VARCHAR(1000) NULL COMMENT '拜访内容' AFTER address,
  ADD COLUMN photos TEXT NULL COMMENT '现场图片，JSON 数组' AFTER content;
