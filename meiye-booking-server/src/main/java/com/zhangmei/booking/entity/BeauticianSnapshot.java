package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_beautician")
public class BeauticianSnapshot extends SnapshotEntity {

    private Long storeId;
    private String name;
    private String level;
    private String avatar;
    private LocalDate entryDate;
    private String status;
}
