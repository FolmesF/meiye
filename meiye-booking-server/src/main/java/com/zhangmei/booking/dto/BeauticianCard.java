package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BeauticianCard {

    private Long id;
    private Long storeId;
    private String name;
    private String avatar;
    private String level;
    /** 由入职日期计算，没有入职日期时为空。 */
    private Integer years;
    /** 评价均分，保留 1 位；没有评价时为空。 */
    private BigDecimal score;
    private int reviewCount;
    private List<String> skills = new ArrayList<>();
    /** OPEN 可约 / FULL 已满 / OFF 当天未排班。 */
    private String bookable;
}
