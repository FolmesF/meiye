package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_campaign")
public class Campaign extends LogicEntity {

    private String name;
    private String campaignType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String ruleText;
}
