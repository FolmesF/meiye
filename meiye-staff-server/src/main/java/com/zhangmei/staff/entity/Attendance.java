package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_attendance")
public class Attendance extends BaseEntity {

    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime clockInAt;
    private BigDecimal clockInLng;
    private BigDecimal clockInLat;
    private LocalDateTime clockOutAt;
    private BigDecimal clockOutLng;
    private BigDecimal clockOutLat;
}
