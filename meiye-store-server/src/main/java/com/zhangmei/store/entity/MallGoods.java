package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_mall_goods")
public class MallGoods extends LogicEntity {

    private String name;
    private String category;
    private BigDecimal price;
    private String brand;
    private String spec;
    private String supplier;
    private String status;
}
