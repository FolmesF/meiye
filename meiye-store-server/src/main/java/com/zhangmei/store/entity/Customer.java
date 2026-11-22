package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class Customer extends LogicEntity {

    private String name;
    private String mobile;
    private String accountNo;
    private String gender;
    private LocalDate birthday;
    private String level;
    private String source;
    private String address;
    private String remark;
    /** ACTIVE 正常，DISABLED 禁用。 */
    private String status;
    private BigDecimal totalAmount;
    private Integer visitCount;
    private LocalDateTime lastVisitAt;
}
