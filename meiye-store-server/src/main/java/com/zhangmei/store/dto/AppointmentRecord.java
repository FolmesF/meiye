package com.zhangmei.store.dto;

import lombok.Data;

@Data
public class AppointmentRecord {

    private Long id;
    private String phase;
    private String appointDate;
    private String timeRange;
    private String customerName;
    private String accountNo;
    private String mobile;
    private String serviceName;
    private String beauticianName;
    /** 是否已取消。 */
    private boolean cancelled;
    /** 结束时间已过。 */
    private boolean expired;
}
