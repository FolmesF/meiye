package com.zhangmei.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewItem {

    private Long id;
    private String nickname;
    private Integer beauticianScore;
    private Integer storeScore;
    private Integer serviceScore;
    private String content;
    private LocalDateTime createdAt;
}
