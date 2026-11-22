package com.zhangmei.store.dto;

import com.zhangmei.store.entity.Customer;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDetail {

    private Customer customer;
    private List<String> tags;
}
