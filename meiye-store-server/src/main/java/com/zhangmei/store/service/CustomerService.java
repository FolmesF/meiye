package com.zhangmei.store.service;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.CustomerDetail;
import com.zhangmei.store.dto.CustomerForms;
import com.zhangmei.store.dto.CustomerQuery;
import com.zhangmei.store.dto.CustomerRow;
import com.zhangmei.store.entity.Consumption;

import java.util.List;

public interface CustomerService {

    Long create(CustomerForms.Save request);

    void syncProfile(CustomerForms.ProfileSync request);

    void ensureRegistered(CustomerForms.RegisterSync request);

    void update(Long id, CustomerForms.Update request);

    void delete(Long id);

    void deleteBatch(Long storeId, List<Long> ids);

    void changeStatus(Long id, String status);

    CustomerDetail detail(Long id);

    PageResult<CustomerRow> page(CustomerQuery query);

    void addTag(Long customerId, String tagName);

    void removeTag(Long customerId, String tagName);

    List<String> listTags(Long customerId);

    Long addConsumption(Long customerId, CustomerForms.ConsumptionCreate request);

    List<Consumption> listConsumptions(Long customerId);
}
