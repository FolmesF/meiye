package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_stocktake")
public class Stocktake extends PersistEntity {

    private Long productId;
    private Integer bookQty;
    private Integer actualQty;
    private Integer diffQty;
    private String remark;
}
