package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_product")
public class Product extends LogicEntity {

    private String name;
    private String skuCode;
    private String unit;
    private Integer maxQuantity;
    private Integer warnQuantity;
    private String status;
    private String remark;
}
