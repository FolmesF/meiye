package com.zhangmei.booking.notify;

import lombok.Data;

@Data
public class NotifyCommand {

    private Long userId;
    private String type;
    private String title;
    private String content;
    private Long bizId;
}
