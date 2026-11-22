package com.zhangmei.store.service;

import com.zhangmei.store.common.BizException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManualFeeCalculatorTest {

    @Test
    void fixedAmountIgnoresPerformance() {
        BigDecimal fee = ManualFeeCalculator.calculate("FIXED", new BigDecimal("88.5"), null, null);
        assertEquals(new BigDecimal("88.50"), fee);
    }

    @Test
    void rateUsesPerformance() {
        BigDecimal fee = ManualFeeCalculator.calculate("RATE", null, new BigDecimal("0.1500"), new BigDecimal("200"));
        assertEquals(new BigDecimal("30.00"), fee);
    }

    @Test
    void rateRoundsHalfUp() {
        BigDecimal fee = ManualFeeCalculator.calculate("RATE", null, new BigDecimal("0.3333"), new BigDecimal("1"));
        assertEquals(new BigDecimal("0.33"), fee);
    }

    @Test
    void rejectsRateAboveOne() {
        assertThrows(BizException.class,
                () -> ManualFeeCalculator.calculate("RATE", null, new BigDecimal("1.01"), new BigDecimal("100")));
    }

    @Test
    void rejectsNegativeFixedAmount() {
        assertThrows(BizException.class,
                () -> ManualFeeCalculator.calculate("FIXED", new BigDecimal("-1"), null, null));
    }

    @Test
    void rejectsMissingPerformanceForRate() {
        assertThrows(BizException.class,
                () -> ManualFeeCalculator.calculate("RATE", null, new BigDecimal("0.1"), null));
    }
}
