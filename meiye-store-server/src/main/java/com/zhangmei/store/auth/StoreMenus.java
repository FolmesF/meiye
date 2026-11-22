package com.zhangmei.store.auth;

import com.zhangmei.store.dto.MenuItem;

import java.util.Arrays;
import java.util.List;

public final class StoreMenus {

    private StoreMenus() {
    }

    public static final List<MenuItem> ALL = List.of(
            item("customer", "/customers", "顾客管理", "User"),
            item("beautician", "/beauticians", "美容师管理", "Avatar"),
            item("service", "/services", "项目管理", "Goods"),
            item("appointment-stat", "/stats", "预约统计", "DataLine"),
            item("manual-fee", "/fees", "手工费核算", "Coin"),
            item("inventory", "/inventory", "库存管理", "Box"),
            item("mall", "/mall", "美博城", "ShoppingBag"),
            item("campaign", "/campaigns", "营销活动", "Present")
    );

    public static List<MenuItem> byCodes(String... codes) {
        List<String> allowed = Arrays.asList(codes);
        return ALL.stream().filter(item -> allowed.contains(item.getCode())).toList();
    }

    private static MenuItem item(String code, String path, String title, String icon) {
        return new MenuItem(code, path, title, icon);
    }
}
