package com.zhangmei.store.service;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.ServiceCard;
import com.zhangmei.store.dto.ServiceItemForms;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.entity.ServiceMaterial;

import java.util.List;

public interface ServiceItemService {

    Long create(ServiceItemForms.Save request);

    void update(Long id, ServiceItemForms.Update request);

    void delete(Long id);

    ServiceItem detail(Long id);

    PageResult<ServiceItem> page(Long storeId, String status, String keyword, long page, long size);

    PageResult<ServiceCard> cards(Long storeId, long page, long size);

    ServiceCard card(Long id);

    void replaceBeauticians(Long serviceItemId, List<Long> beauticianIds);

    void publishAll();

    void replaceMaterials(Long serviceItemId, List<ServiceItemForms.Line> materials);

    List<ServiceMaterial> listMaterials(Long serviceItemId);
}
