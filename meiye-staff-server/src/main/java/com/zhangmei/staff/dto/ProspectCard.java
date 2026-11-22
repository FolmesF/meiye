package com.zhangmei.staff.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProspectCard {

    private Long id;
    private String name;
    private String status;
    private LocalDateTime visitedAt;
    private String remark;
}
