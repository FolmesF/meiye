package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_beautician_service")
public class BeauticianServiceBind extends PersistEntity {

    private Long beauticianId;
    private Long serviceItemId;
}
