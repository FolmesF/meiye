package com.zhangmei.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItem {

    private String code;
    private String path;
    private String title;
    /** 与前端 Element Plus 图标组件名一致。 */
    private String icon;
}
