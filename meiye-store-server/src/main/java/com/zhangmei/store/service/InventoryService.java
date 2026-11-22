package com.zhangmei.store.service;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.InventoryForms;
import com.zhangmei.store.dto.InventorySummaryRow;
import com.zhangmei.store.dto.LowStockRow;
import com.zhangmei.store.entity.Product;
import com.zhangmei.store.entity.Stock;
import com.zhangmei.store.entity.StockFlow;
import com.zhangmei.store.entity.Stocktake;

import java.util.List;

public interface InventoryService {

    Long createProduct(InventoryForms.ProductSave request);

    void updateProduct(Long id, InventoryForms.ProductUpdate request);

    void deleteProduct(Long id);

    PageResult<Product> pageProducts(Long storeId, String keyword, long page, long size);

    Stock inbound(InventoryForms.StockChange request);

    Stock outbound(InventoryForms.StockChange request);

    Stocktake stocktake(InventoryForms.StocktakeCreate request);

    List<Stock> listStocks(Long storeId);

    List<StockFlow> listFlows(Long storeId, Long productId);

    List<LowStockRow> lowStock(Long storeId);

    List<InventorySummaryRow> summary(Long storeId);

    void updateQuantity(Long productId, InventoryForms.QuantityUpdate request);

    void updateMaxQuantity(Long productId, int maxQuantity);

    void consumeForService(Long storeId, Long serviceItemId, Long appointmentId);
}
