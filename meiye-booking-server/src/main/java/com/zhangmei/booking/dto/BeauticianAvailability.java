package com.zhangmei.booking.dto;

import lombok.Data;

import java.util.List;

@Data
public class BeauticianAvailability {

    private Long beauticianId;
    private String name;
    private String level;
    private String avatar;
    private List<String> slots;
}
