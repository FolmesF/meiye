package com.zhangmei.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.store.dto.LowStockRow;
import com.zhangmei.store.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("""
            SELECT p.id AS product_id,
                   p.store_id,
                   p.name AS product_name,
                   p.sku_code,
                   p.unit,
                   p.warn_quantity,
                   IFNULL(s.quantity, 0) AS quantity
            FROM inv_product p
            LEFT JOIN inv_stock s ON s.product_id = p.id AND s.store_id = p.store_id
            WHERE p.deleted = 0
              AND p.store_id = #{storeId}
              AND IFNULL(s.quantity, 0) <= p.warn_quantity
            ORDER BY quantity ASC, p.id ASC
            """)
    List<LowStockRow> selectLowStock(@Param("storeId") Long storeId);

    @Select("""
            <script>
            SELECT p.id AS product_id,
                   p.store_id,
                   p.name AS product_name,
                   p.sku_code,
                   p.unit,
                   p.warn_quantity,
                   IFNULL(s.quantity, 0) AS quantity
            FROM inv_product p
            LEFT JOIN inv_stock s ON s.product_id = p.id AND s.store_id = p.store_id
            WHERE p.deleted = 0
              AND IFNULL(s.quantity, 0) &lt;= p.warn_quantity
            <if test="storeId != null">
              AND p.store_id = #{storeId}
            </if>
            ORDER BY quantity ASC, p.id ASC
            </script>
            """)
    List<LowStockRow> selectLowStockOptional(@Param("storeId") Long storeId);
}
