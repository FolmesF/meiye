package com.zhangmei.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.store.dto.CustomerRow;
import com.zhangmei.store.entity.Customer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerMapper extends BaseMapper<Customer> {

    List<CustomerRow> selectCustomerRows(@Param("storeId") Long storeId,
                                          @Param("keyword") String keyword,
                                          @Param("level") String level,
                                          @Param("createdFrom") LocalDateTime createdFrom,
                                          @Param("createdTo") LocalDateTime createdTo,
                                          @Param("offset") long offset,
                                          @Param("limit") long limit);

    long countCustomerRows(@Param("storeId") Long storeId,
                            @Param("keyword") String keyword,
                            @Param("level") String level,
                            @Param("createdFrom") LocalDateTime createdFrom,
                            @Param("createdTo") LocalDateTime createdTo);

    @Update("""
            UPDATE biz_customer
            SET total_amount = total_amount + #{amount},
                visit_count = visit_count + 1,
                updated_at = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int addConsumptionStat(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
