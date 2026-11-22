package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_service_item")
public class ServiceSnapshot extends SnapshotEntity {

    private Long storeId;
    private String name;
    private BigDecimal price;
    private Integer durationMin;
    private String status;
}
