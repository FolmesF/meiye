package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer_tag")
public class CustomerTag extends PersistEntity {

    private Long customerId;
    private String tagName;
}
