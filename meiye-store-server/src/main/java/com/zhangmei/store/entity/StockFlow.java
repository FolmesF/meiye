package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_stock_flow")
public class StockFlow extends PersistEntity {

    private Long productId;
    private String bizType;
    private Integer changeQty;
    private Integer beforeQty;
    private Integer afterQty;
    private Long bizId;
    private String remark;
}
