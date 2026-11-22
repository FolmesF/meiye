package com.zhangmei.store.service;

import com.zhangmei.store.dto.ManualFeeForms;
import com.zhangmei.store.entity.Appointment;
import com.zhangmei.store.entity.ManualFeeRecord;
import com.zhangmei.store.entity.ManualFeeRule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ManualFeeService {

    Long saveRule(ManualFeeForms.RuleSave request);

    List<ManualFeeRule> listRules(Long storeId, Long serviceItemId);

    BigDecimal preview(ManualFeeForms.Preview request);

    ManualFeeRecord settle(Long appointmentId);

    ManualFeeRecord settle(Appointment appointment);

    List<ManualFeeRecord> listRecords(Long storeId, Long beauticianId, LocalDate startDate, LocalDate endDate);
}
