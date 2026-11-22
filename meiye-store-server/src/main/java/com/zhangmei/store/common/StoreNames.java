package com.zhangmei.store.common;

import java.util.Map;

public final class StoreNames {

    private static final Map<Long, String> NAMES = Map.of(
            1L, "掌美西湖旗舰店",
            2L, "掌美湖墅店",
            3L, "掌美延安店",
            4L, "掌美滨江店",
            5L, "掌美未来科技城店"
    );

    private StoreNames() {
    }

    public static String of(Long storeId) {
        if (storeId == null) {
            return "门店";
        }
        return NAMES.getOrDefault(storeId, "门店" + storeId);
    }
}
