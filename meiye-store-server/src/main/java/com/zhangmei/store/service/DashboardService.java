package com.zhangmei.store.service;

import com.zhangmei.store.common.StoreNames;
import com.zhangmei.store.dto.AppointmentStatQuery;
import com.zhangmei.store.dto.AppointmentStatResponse;
import com.zhangmei.store.dto.DashboardOverview;
import com.zhangmei.store.mapper.ConsumptionMapper;
import com.zhangmei.store.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentService appointmentService;
    private final ConsumptionMapper consumptionMapper;
    private final ProductMapper productMapper;

    public DashboardOverview overview(Long storeId) {
        LocalDate today = LocalDate.now();
        AppointmentStatQuery query = new AppointmentStatQuery();
        query.setStoreId(storeId);
        query.setDimension("DAY");
        query.setStartDate(today);
        query.setEndDate(today);
        List<AppointmentStatResponse> stats = appointmentService.stats(query);

        DashboardOverview overview = new DashboardOverview();
        overview.setStoreName(StoreNames.of(storeId));
        if (!stats.isEmpty()) {
            AppointmentStatResponse row = stats.get(0);
            overview.setAppointmentCount(row.getAppointmentCount());
            overview.setArrivedCount(row.getArrivedCount());
        }
        LocalDateTime start = today.atStartOfDay();
        BigDecimal revenue = consumptionMapper.sumAmount(storeId, start, start.plusDays(1));
        overview.setRevenue(revenue == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : revenue.setScale(2, RoundingMode.HALF_UP));
        overview.setAlerts(productMapper.selectLowStockOptional(storeId));
        return overview;
    }
}
