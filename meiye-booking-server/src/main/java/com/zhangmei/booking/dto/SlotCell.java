package com.zhangmei.booking.dto;

import lombok.Data;

@Data
public class SlotCell {

    /** HH:mm */
    private String start;
    /** OPEN 可约 / FULL 已满 / MINE 当前用户已约。 */
    private String status;
}
