package com.zhangmei.store.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceCard {

    private Long id;
    private String name;
    private Integer durationMin;
    private BigDecimal price;
    private String introduction;
    private String beauticians;
    private List<Long> beauticianIds = new ArrayList<>();
    private List<MaterialLine> materials = new ArrayList<>();

    @Data
    public static class MaterialLine {
        private String productName;
        private BigDecimal quantity;
    }
}
