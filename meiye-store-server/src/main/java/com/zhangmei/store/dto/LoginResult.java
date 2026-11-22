package com.zhangmei.store.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResult {

    private String token;
    private String name;
    private String mobile;
    private Long storeId;
    private String storeName;
    private List<MenuItem> menus;
}
