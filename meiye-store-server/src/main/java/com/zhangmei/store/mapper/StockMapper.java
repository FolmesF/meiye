package com.zhangmei.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.store.entity.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    @Update("""
            UPDATE inv_stock
            SET quantity = quantity + #{qty}, updated_at = NOW()
            WHERE id = #{id}
            """)
    int increase(@Param("id") Long id, @Param("qty") int qty);

    @Update("""
            UPDATE inv_stock
            SET quantity = quantity - #{qty}, updated_at = NOW()
            WHERE id = #{id} AND quantity >= #{qty}
            """)
    int deduct(@Param("id") Long id, @Param("qty") int qty);

    @Update("""
            UPDATE inv_stock
            SET quantity = quantity - #{qty},
                today_consumed = IF(consume_date = CURDATE(), IFNULL(today_consumed, 0), 0) + #{qty},
                total_consumed = IFNULL(total_consumed, 0) + #{qty},
                consume_date = CURDATE(),
                updated_at = NOW()
            WHERE id = #{id} AND quantity >= #{qty}
            """)
    int consume(@Param("id") Long id, @Param("qty") int qty);

    @Update("""
            UPDATE inv_stock
            SET quantity = #{actualQty}, updated_at = NOW()
            WHERE id = #{id} AND quantity = #{bookQty}
            """)
    int compareAndSet(@Param("id") Long id, @Param("bookQty") int bookQty, @Param("actualQty") int actualQty);
}
