package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.store.booking.BookingClient;
import com.zhangmei.store.common.AccountNo;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.CustomerDetail;
import com.zhangmei.store.dto.CustomerForms;
import com.zhangmei.store.dto.CustomerQuery;
import com.zhangmei.store.dto.CustomerRow;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.Consumption;
import com.zhangmei.store.entity.Customer;
import com.zhangmei.store.entity.CustomerTag;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.mapper.BeauticianMapper;
import com.zhangmei.store.mapper.ConsumptionMapper;
import com.zhangmei.store.mapper.CustomerMapper;
import com.zhangmei.store.mapper.CustomerTagMapper;
import com.zhangmei.store.mapper.ServiceItemMapper;
import com.zhangmei.store.service.Checks;
import com.zhangmei.store.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerTagMapper customerTagMapper;
    private final ConsumptionMapper consumptionMapper;
    private final BeauticianMapper beauticianMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BookingClient bookingClient;

    @Override
    public Long create(CustomerForms.Save request) {
        Checks.gender(request.getGender());
        assertMobileFree(request.getStoreId(), request.getMobile(), null);
        Customer customer = new Customer();
        customer.setStoreId(request.getStoreId());
        customer.setName(request.getName().trim());
        customer.setMobile(request.getMobile());
        customer.setAccountNo(AccountNo.of(request.getMobile()));
        customer.setGender(blankToNull(request.getGender()));
        customer.setBirthday(request.getBirthday());
        customer.setLevel(blankToNull(request.getLevel()));
        customer.setSource(blankToNull(request.getSource()));
        customer.setAddress(blankToNull(request.getAddress()));
        customer.setRemark(blankToNull(request.getRemark()));
        customer.setStatus("ACTIVE");
        customer.setTotalAmount(java.math.BigDecimal.ZERO);
        customer.setVisitCount(0);
        customerMapper.insert(customer);
        bookingClient.syncMemberLevel(customer.getMobile(), customer.getLevel());
        return customer.getId();
    }

    @Override
    @Transactional
    public void syncProfile(CustomerForms.ProfileSync request) {
        Checks.gender(request.getGender());
        List<Customer> customers = customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getAccountNo, request.getAccountNo()));
        if (customers.isEmpty()) {
            customers = customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getMobile, request.getMobile())
                    .and(wrapper -> wrapper.isNull(Customer::getAccountNo).or().eq(Customer::getAccountNo, "")));
        }
        if (customers.isEmpty()) {
            Customer created = new Customer();
            created.setStoreId(1L);
            created.setName(request.getName().trim());
            created.setMobile(request.getMobile());
            created.setAccountNo(request.getAccountNo());
            created.setGender(blankToNull(request.getGender()));
            created.setBirthday(request.getBirthday());
            created.setAddress(blankToNull(request.getAddress()));
            created.setLevel(blankToNull(request.getMemberLevel()));
            created.setSource("预约端");
            created.setStatus("ACTIVE");
            created.setTotalAmount(java.math.BigDecimal.ZERO);
            created.setVisitCount(0);
            customerMapper.insert(created);
            replaceTags(created.getId(), created.getStoreId(), request.getTags());
            return;
        }
        for (Customer customer : customers) {
            customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                    .eq(Customer::getId, customer.getId())
                    .set(Customer::getAccountNo, request.getAccountNo())
                    .set(Customer::getName, request.getName().trim())
                    .set(Customer::getMobile, request.getMobile())
                    .set(Customer::getGender, blankToNull(request.getGender()))
                    .set(Customer::getBirthday, request.getBirthday())
                    .set(Customer::getAddress, blankToNull(request.getAddress())));
            replaceTags(customer.getId(), customer.getStoreId(), request.getTags());
        }
    }

    @Override
    public void ensureRegistered(CustomerForms.RegisterSync request) {
        Customer existing = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getAccountNo, request.getAccountNo())
                .last("limit 1"));
        if (existing == null) {
            existing = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                    .eq(Customer::getStoreId, 1L)
                    .eq(Customer::getMobile, request.getMobile())
                    .last("limit 1"));
        }
        if (existing != null) {
            if (existing.getAccountNo() == null || existing.getAccountNo().isBlank()) {
                customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                        .eq(Customer::getId, existing.getId())
                        .set(Customer::getAccountNo, request.getAccountNo()));
            }
            return;
        }
        Customer created = new Customer();
        created.setStoreId(1L);
        created.setName(request.getName().trim());
        created.setMobile(request.getMobile());
        created.setAccountNo(request.getAccountNo());
        created.setLevel(blankToNull(request.getMemberLevel()));
        created.setSource("预约端");
        created.setStatus("ACTIVE");
        created.setTotalAmount(java.math.BigDecimal.ZERO);
        created.setVisitCount(0);
        customerMapper.insert(created);
    }

    @Override
    public void update(Long id, CustomerForms.Update request) {
        Customer customer = mustGet(id);
        Checks.gender(request.getGender());
        assertMobileFree(customer.getStoreId(), request.getMobile(), id);
        customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                .eq(Customer::getId, id)
                .set(Customer::getName, request.getName().trim())
                .set(Customer::getMobile, request.getMobile())
                .set(Customer::getGender, blankToNull(request.getGender()))
                .set(Customer::getBirthday, request.getBirthday())
                .set(Customer::getLevel, blankToNull(request.getLevel()))
                .set(Customer::getAddress, blankToNull(request.getAddress()))
                .set(Customer::getRemark, blankToNull(request.getRemark())));
        bookingClient.syncMemberLevel(request.getMobile(), request.getLevel());
    }

    @Override
    public void delete(Long id) {
        mustGet(id);
        customerMapper.deleteById(id);
    }

    @Override
    public void deleteBatch(Long storeId, List<Long> ids) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        if (ids == null || ids.isEmpty() || ids.size() > 100) {
            throw new BizException("请选择 1 到 100 名顾客");
        }
        customerMapper.delete(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getStoreId, storeId)
                .in(Customer::getId, ids));
    }

    @Override
    public void changeStatus(Long id, String status) {
        Checks.customerStatus(status);
        mustGet(id);
        customerMapper.update(null, new LambdaUpdateWrapper<Customer>()
                .eq(Customer::getId, id)
                .set(Customer::getStatus, status));
    }

    @Override
    public CustomerDetail detail(Long id) {
        CustomerDetail detail = new CustomerDetail();
        detail.setCustomer(mustGet(id));
        detail.setTags(listTags(id));
        return detail;
    }

    @Override
    public PageResult<CustomerRow> page(CustomerQuery query) {
        if (query.getStoreId() == null) {
            throw new BizException("门店不能为空");
        }
        if (query.getCreatedFrom() != null && query.getCreatedTo() != null
                && query.getCreatedTo().isBefore(query.getCreatedFrom())) {
            throw new BizException("注册结束日期不能早于开始日期");
        }
        long current = Checks.page(query.getPage());
        long pageSize = Checks.size(query.getSize());
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        String level = StringUtils.hasText(query.getLevel()) ? query.getLevel().trim() : null;
        LocalDateTime createdFrom = query.getCreatedFrom() == null ? null : query.getCreatedFrom().atStartOfDay();
        LocalDateTime createdTo = query.getCreatedTo() == null ? null : query.getCreatedTo().plusDays(1).atStartOfDay();
        long total = customerMapper.countCustomerRows(query.getStoreId(), keyword, level, createdFrom, createdTo);
        List<CustomerRow> records = total == 0 ? List.of() : customerMapper.selectCustomerRows(
                query.getStoreId(), keyword, level, createdFrom, createdTo, (current - 1) * pageSize, pageSize);
        return PageResult.of(total, current, pageSize, records);
    }

    @Override
    public void addTag(Long customerId, String tagName) {
        Customer customer = mustGet(customerId);
        String tag = tagName.trim();
        Long exists = customerTagMapper.selectCount(new LambdaQueryWrapper<CustomerTag>()
                .eq(CustomerTag::getCustomerId, customerId)
                .eq(CustomerTag::getTagName, tag));
        if (exists > 0) {
            return;
        }
        CustomerTag row = new CustomerTag();
        row.setStoreId(customer.getStoreId());
        row.setCustomerId(customerId);
        row.setTagName(tag);
        customerTagMapper.insert(row);
    }

    @Override
    public void removeTag(Long customerId, String tagName) {
        mustGet(customerId);
        customerTagMapper.delete(new LambdaQueryWrapper<CustomerTag>()
                .eq(CustomerTag::getCustomerId, customerId)
                .eq(CustomerTag::getTagName, tagName));
    }

    @Override
    public List<String> listTags(Long customerId) {
        mustGet(customerId);
        return customerTagMapper.selectList(new LambdaQueryWrapper<CustomerTag>()
                        .eq(CustomerTag::getCustomerId, customerId)
                        .orderByAsc(CustomerTag::getId))
                .stream()
                .map(CustomerTag::getTagName)
                .toList();
    }

    @Override
    @Transactional
    public Long addConsumption(Long customerId, CustomerForms.ConsumptionCreate request) {
        Customer customer = mustGet(customerId);
        if (request.getBeauticianId() != null) {
            Beautician beautician = beauticianMapper.selectById(request.getBeauticianId());
            if (beautician == null || !beautician.getStoreId().equals(customer.getStoreId())) {
                throw new BizException("美容师不存在或不属于该门店");
            }
        }
        if (request.getServiceItemId() != null) {
            ServiceItem item = serviceItemMapper.selectById(request.getServiceItemId());
            if (item == null || !item.getStoreId().equals(customer.getStoreId())) {
                throw new BizException("服务项目不存在或不属于该门店");
            }
        }
        Consumption consumption = new Consumption();
        consumption.setStoreId(customer.getStoreId());
        consumption.setCustomerId(customerId);
        consumption.setBeauticianId(request.getBeauticianId());
        consumption.setServiceItemId(request.getServiceItemId());
        consumption.setAppointmentId(request.getAppointmentId());
        consumption.setAmount(request.getAmount());
        consumption.setConsumedAt(request.getConsumedAt());
        consumption.setRemark(request.getRemark());
        consumptionMapper.insert(consumption);
        if (customerMapper.addConsumptionStat(customerId, request.getAmount()) != 1) {
            throw new BizException("更新顾客消费汇总失败");
        }
        return consumption.getId();
    }

    @Override
    public List<Consumption> listConsumptions(Long customerId) {
        mustGet(customerId);
        return consumptionMapper.selectList(new LambdaQueryWrapper<Consumption>()
                .eq(Consumption::getCustomerId, customerId)
                .orderByDesc(Consumption::getConsumedAt)
                .orderByDesc(Consumption::getId));
    }

    private Customer mustGet(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BizException(404, "顾客不存在");
        }
        return customer;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void replaceTags(Long customerId, Long storeId, List<String> tags) {
        customerTagMapper.delete(new LambdaQueryWrapper<CustomerTag>().eq(CustomerTag::getCustomerId, customerId));
        if (tags == null) {
            return;
        }
        for (String raw : tags) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            CustomerTag tag = new CustomerTag();
            tag.setStoreId(storeId);
            tag.setCustomerId(customerId);
            tag.setTagName(raw.trim());
            customerTagMapper.insert(tag);
        }
    }

    private void assertMobileFree(Long storeId, String mobile, Long excludeId) {
        Long count = customerMapper.selectCount(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getStoreId, storeId)
                .eq(Customer::getMobile, mobile)
                .ne(excludeId != null, Customer::getId, excludeId));
        if (count > 0) {
            throw new BizException(409, "该门店已存在相同手机号的顾客");
        }
    }
}
