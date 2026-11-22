package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bk_review")
public class Review extends BaseEntity {

    private Long userId;
    private Long appointmentId;
    private Long storeId;
    private Long beauticianId;
    private Long serviceItemId;
    private Integer beauticianScore;
    private Integer storeScore;
    private Integer serviceScore;
    private String content;
}
