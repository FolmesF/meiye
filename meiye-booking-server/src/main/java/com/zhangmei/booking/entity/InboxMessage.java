package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bk_message")
public class InboxMessage extends BaseEntity {

    private Long userId;
    private String msgType;
    private String title;
    private String content;
    private Long bizId;
    private Integer readFlag;
    private String channel;
}
