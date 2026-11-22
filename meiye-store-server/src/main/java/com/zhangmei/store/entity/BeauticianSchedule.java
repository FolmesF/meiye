package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_beautician_schedule")
public class BeauticianSchedule extends PersistEntity {

    private Long beauticianId;
    private LocalDate workDate;
    private String shiftStatus;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private String remark;
}
