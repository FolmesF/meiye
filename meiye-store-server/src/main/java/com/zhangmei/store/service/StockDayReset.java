package com.zhangmei.store.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.store.entity.Stock;
import com.zhangmei.store.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StockDayReset {

    private final StockMapper stockMapper;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")
    public void resetTodayConsumed() {
        stockMapper.update(null, new LambdaUpdateWrapper<Stock>()
                .isNotNull(Stock::getConsumeDate)
                .lt(Stock::getConsumeDate, LocalDate.now())
                .set(Stock::getTodayConsumed, 0));
    }
}
