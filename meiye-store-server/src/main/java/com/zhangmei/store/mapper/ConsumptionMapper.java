package com.zhangmei.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.store.entity.Consumption;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ConsumptionMapper extends BaseMapper<Consumption> {

    @Select("""
            <script>
            SELECT COALESCE(SUM(amount), 0)
            FROM biz_consumption
            WHERE consumed_at &gt;= #{start}
              AND consumed_at &lt; #{end}
            <if test="storeId != null">
              AND store_id = #{storeId}
            </if>
            </script>
            """)
    BigDecimal sumAmount(@Param("storeId") Long storeId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}
