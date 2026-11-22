package com.zhangmei.staff.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamStatusRow {

    private Long employeeId;
    private String name;
    private String mobile;
    private String jobType;
    private String status;
    private LocalDateTime clockInAt;
    private LocalDateTime clockOutAt;
    private LocalDateTime lastReportAt;
    private String lastReportContent;
}
