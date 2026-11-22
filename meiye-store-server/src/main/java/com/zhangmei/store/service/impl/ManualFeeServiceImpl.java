package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.dto.ManualFeeForms;
import com.zhangmei.store.entity.Appointment;
import com.zhangmei.store.entity.ManualFeeRecord;
import com.zhangmei.store.entity.ManualFeeRule;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.mapper.AppointmentMapper;
import com.zhangmei.store.mapper.ManualFeeRecordMapper;
import com.zhangmei.store.mapper.ManualFeeRuleMapper;
import com.zhangmei.store.mapper.ServiceItemMapper;
import com.zhangmei.store.service.ManualFeeCalculator;
import com.zhangmei.store.service.ManualFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualFeeServiceImpl implements ManualFeeService {

    private final ManualFeeRuleMapper ruleMapper;
    private final ManualFeeRecordMapper recordMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public Long saveRule(ManualFeeForms.RuleSave request) {
        ServiceItem item = serviceItemMapper.selectById(request.getServiceItemId());
        if (item == null || !item.getStoreId().equals(request.getStoreId())) {
            throw new BizException("服务项目不存在或不属于该门店");
        }
        validateRule(request.getRuleType(), request.getFixedAmount(), request.getRate());
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BizException("规则失效日期不能早于生效日期");
        }
        ruleMapper.update(null, new LambdaUpdateWrapper<ManualFeeRule>()
                .eq(ManualFeeRule::getStoreId, request.getStoreId())
                .eq(ManualFeeRule::getServiceItemId, request.getServiceItemId())
                .eq(ManualFeeRule::getEnabled, 1)
                .set(ManualFeeRule::getEnabled, 0));
        ManualFeeRule rule = new ManualFeeRule();
        rule.setStoreId(request.getStoreId());
        rule.setServiceItemId(request.getServiceItemId());
        rule.setRuleType(request.getRuleType());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setRate(request.getRate());
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        rule.setEnabled(1);
        ruleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public List<ManualFeeRule> listRules(Long storeId, Long serviceItemId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        return ruleMapper.selectList(new LambdaQueryWrapper<ManualFeeRule>()
                .eq(ManualFeeRule::getStoreId, storeId)
                .eq(serviceItemId != null, ManualFeeRule::getServiceItemId, serviceItemId)
                .orderByDesc(ManualFeeRule::getEffectiveFrom)
                .orderByDesc(ManualFeeRule::getId));
    }

    @Override
    public BigDecimal preview(ManualFeeForms.Preview request) {
        ManualFeeRule rule = currentRule(request.getStoreId(), request.getServiceItemId(), LocalDate.now());
        return ManualFeeCalculator.calculate(
                rule.getRuleType(), rule.getFixedAmount(), rule.getRate(), request.getPerformanceAmount());
    }

    @Override
    @Transactional
    public ManualFeeRecord settle(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new BizException("仅已完成的预约可核算手工费");
        }
        return settle(appointment);
    }

    @Override
    @Transactional
    public ManualFeeRecord settle(Appointment appointment) {
        ManualFeeRecord existing = recordMapper.selectOne(new LambdaQueryWrapper<ManualFeeRecord>()
                .eq(ManualFeeRecord::getAppointmentId, appointment.getId()));
        if (existing != null) {
            return existing;
        }
        LocalDate serviceDate = appointment.getAppointTime().toLocalDate();
        ManualFeeRule rule = currentRule(appointment.getStoreId(), appointment.getServiceItemId(), serviceDate);
        BigDecimal fee = ManualFeeCalculator.calculate(
                rule.getRuleType(), rule.getFixedAmount(), rule.getRate(), appointment.getAmount());
        ManualFeeRecord record = new ManualFeeRecord();
        record.setStoreId(appointment.getStoreId());
        record.setBeauticianId(appointment.getBeauticianId());
        record.setAppointmentId(appointment.getId());
        record.setServiceItemId(appointment.getServiceItemId());
        record.setRuleId(rule.getId());
        record.setRuleType(rule.getRuleType());
        record.setFixedAmount(rule.getFixedAmount());
        record.setRate(rule.getRate());
        record.setBaseAmount(appointment.getAmount());
        record.setFeeAmount(fee);
        recordMapper.insert(record);
        return record;
    }

    @Override
    public List<ManualFeeRecord> listRecords(Long storeId, Long beauticianId, LocalDate startDate, LocalDate endDate) {
        if (storeId == null || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BizException("手工费查询条件无效");
        }
        return recordMapper.selectList(new LambdaQueryWrapper<ManualFeeRecord>()
                .eq(ManualFeeRecord::getStoreId, storeId)
                .eq(beauticianId != null, ManualFeeRecord::getBeauticianId, beauticianId)
                .ge(ManualFeeRecord::getCreatedAt, startDate.atStartOfDay())
                .lt(ManualFeeRecord::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .orderByDesc(ManualFeeRecord::getId));
    }

    private ManualFeeRule currentRule(Long storeId, Long serviceItemId, LocalDate day) {
        return ruleMapper.selectList(new LambdaQueryWrapper<ManualFeeRule>()
                        .eq(ManualFeeRule::getStoreId, storeId)
                        .eq(ManualFeeRule::getServiceItemId, serviceItemId)
                        .eq(ManualFeeRule::getEnabled, 1))
                .stream()
                .filter(rule -> within(rule, day))
                .max(Comparator.comparing(ManualFeeRule::getEffectiveFrom))
                .orElseThrow(() -> new BizException("该项目在 " + day + " 没有生效的手工费规则"));
    }

    private boolean within(ManualFeeRule rule, LocalDate day) {
        boolean afterStart = !day.isBefore(rule.getEffectiveFrom());
        boolean beforeEnd = rule.getEffectiveTo() == null || !day.isAfter(rule.getEffectiveTo());
        return afterStart && beforeEnd;
    }

    private void validateRule(String ruleType, BigDecimal fixedAmount, BigDecimal rate) {
        if ("FIXED".equals(ruleType)) {
            if (fixedAmount == null || fixedAmount.signum() < 0) {
                throw new BizException("固定金额规则必须填写不小于 0 的金额");
            }
            return;
        }
        if ("RATE".equals(ruleType)) {
            if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new BizException("业绩比例必须在 0 到 1 之间");
            }
            return;
        }
        throw new BizException("规则类型只允许 FIXED 或 RATE");
    }
}
