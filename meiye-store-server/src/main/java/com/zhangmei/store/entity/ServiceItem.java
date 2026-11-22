package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_item")
public class ServiceItem extends LogicEntity {

    private String name;
    private String category;
    private BigDecimal price;
    private Integer durationMin;
    private String status;
    private String remark;
}
