package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_store")
public class StoreSnapshot extends SnapshotEntity {

    private String name;
    private String phone;
    private String address;
    private String status;
}
