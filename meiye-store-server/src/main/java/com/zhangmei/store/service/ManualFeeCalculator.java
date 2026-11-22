package com.zhangmei.store.service;

import com.zhangmei.store.common.BizException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 手工费只按两种规则计算：项目固定金额，或项目业绩乘以比例。
 * 比例取 0 到 1，0.1500 表示业绩的 15%。结果保留两位小数，四舍五入。
 */
public final class ManualFeeCalculator {

    private ManualFeeCalculator() {
    }

    public static BigDecimal calculate(String ruleType, BigDecimal fixedAmount, BigDecimal rate, BigDecimal performanceAmount) {
        if ("FIXED".equals(ruleType)) {
            if (fixedAmount == null || fixedAmount.signum() < 0) {
                throw new BizException("固定手工费金额无效");
            }
            return fixedAmount.setScale(2, RoundingMode.HALF_UP);
        }
        if ("RATE".equals(ruleType)) {
            if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new BizException("业绩比例需在 0 到 1 之间");
            }
            if (performanceAmount == null || performanceAmount.signum() < 0) {
                throw new BizException("项目业绩金额无效");
            }
            return performanceAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        throw new BizException("未知手工费规则");
    }
}
