package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.dto.PerformanceStatRow;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Performance;
import com.zhangmei.staff.mapper.PerformanceMapper;
import com.zhangmei.staff.mapper.PerformanceStatMapper;
import com.zhangmei.staff.security.StaffContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private static final Set<String> DIMENSIONS = Set.of("DAY", "WEEK", "MONTH");

    private final PerformanceMapper performanceMapper;
    private final PerformanceStatMapper performanceStatMapper;
    private final StoreWorkService storeWorkService;

    public List<PerformanceStatRow> self(StaffForms.PerformanceQuery query) {
        query.setEmployeeId(StaffContext.requireEmployeeId());
        query.setGroupByPerson(true);
        return aggregate(query);
    }

    public List<PerformanceStatRow> store(StaffForms.PerformanceQuery query) {
        if (query.getStoreId() == null) {
            throw new BizException("请选择门店");
        }
        storeWorkService.requireOwned(query.getStoreId());
        return aggregate(query);
    }

    public List<PerformanceStatRow> team(StaffForms.PerformanceQuery query) {
        return aggregate(query);
    }

    public Long record(StaffForms.PerformanceRecord request) {
        if (request.getSourceBizId() != null && !request.getSourceBizId().isBlank()) {
            Performance existing = performanceMapper.selectOne(new LambdaQueryWrapper<Performance>()
                    .eq(Performance::getSourceBizId, request.getSourceBizId()));
            if (existing != null) {
                return existing.getId();
            }
        }
        Performance row = new Performance();
        row.setEmployeeId(request.getEmployeeId());
        row.setStoreId(request.getStoreId());
        row.setBizDate(request.getBizDate());
        row.setAmount(request.getAmount());
        row.setSourceBizId(request.getSourceBizId());
        try {
            performanceMapper.insert(row);
        } catch (DuplicateKeyException exception) {
            Performance existing = performanceMapper.selectOne(new LambdaQueryWrapper<Performance>()
                    .eq(Performance::getSourceBizId, request.getSourceBizId()));
            if (existing != null) {
                return existing.getId();
            }
            throw exception;
        }
        return row.getId();
    }

    private List<PerformanceStatRow> aggregate(StaffForms.PerformanceQuery query) {
        if (!DIMENSIONS.contains(query.getDimension())) {
            throw new BizException("统计维度只允许 DAY、WEEK、MONTH");
        }
        if (query.getEndDate().isBefore(query.getStartDate())) {
            throw new BizException("结束日期不能早于开始日期");
        }
        if (ChronoUnit.DAYS.between(query.getStartDate(), query.getEndDate()) > 366) {
            throw new BizException("统计区间不能超过 366 天");
        }
        return performanceStatMapper.aggregate(
                query.getEmployeeId(),
                query.getStoreId(),
                query.getDimension(),
                query.getStartDate(),
                query.getEndDate(),
                query.isGroupByPerson(),
                query.isGroupByStore());
    }
}
