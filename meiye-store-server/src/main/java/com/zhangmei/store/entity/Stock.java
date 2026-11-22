package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inv_stock")
public class Stock extends PersistEntity {

    private Long productId;
    private Integer quantity;
    private Integer todayConsumed;
    private Integer totalConsumed;
    private LocalDate consumeDate;
}
