package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_schedule")
public class ScheduleSnapshot extends SnapshotEntity {

    private Long storeId;
    private Long beauticianId;
    private LocalDate workDate;
    private String shiftStatus;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
}
