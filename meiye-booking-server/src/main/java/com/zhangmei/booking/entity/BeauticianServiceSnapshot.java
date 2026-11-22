package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_beautician_service")
public class BeauticianServiceSnapshot extends BaseEntity {

    private Long beauticianId;
    private Long serviceItemId;
}
