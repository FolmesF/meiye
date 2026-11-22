package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_service_material")
public class ServiceMaterial extends PersistEntity {

    private Long serviceItemId;
    private Long productId;
    private BigDecimal quantity;
}
