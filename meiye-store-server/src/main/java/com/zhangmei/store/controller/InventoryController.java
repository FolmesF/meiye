package com.zhangmei.store.controller;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.InventoryForms;
import com.zhangmei.store.dto.InventorySummaryRow;
import com.zhangmei.store.dto.LowStockRow;
import com.zhangmei.store.entity.Product;
import com.zhangmei.store.entity.Stock;
import com.zhangmei.store.entity.StockFlow;
import com.zhangmei.store.entity.Stocktake;
import com.zhangmei.store.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/summary")
    public R<List<InventorySummaryRow>> summary(@RequestParam Long storeId) {
        return R.ok(inventoryService.summary(storeId));
    }

    @PutMapping("/products/{id}/quantity")
    public R<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody InventoryForms.QuantityUpdate request) {
        inventoryService.updateQuantity(id, request);
        return R.ok(null);
    }

    @PutMapping("/products/{id}/max")
    public R<Void> updateMax(@PathVariable Long id, @Valid @RequestBody InventoryForms.MaxUpdate request) {
        inventoryService.updateMaxQuantity(id, request.getMaxQuantity());
        return R.ok(null);
    }

    @PostMapping("/products")
    public R<Long> createProduct(@Valid @RequestBody InventoryForms.ProductSave request) {
        return R.ok(inventoryService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public R<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody InventoryForms.ProductUpdate request) {
        inventoryService.updateProduct(id, request);
        return R.ok(null);
    }

    @DeleteMapping("/products/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
        return R.ok(null);
    }

    @GetMapping("/products")
    public R<PageResult<Product>> products(@RequestParam Long storeId,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size) {
        return R.ok(inventoryService.pageProducts(storeId, keyword, page, size));
    }

    @PostMapping("/inbound")
    public R<Stock> inbound(@Valid @RequestBody InventoryForms.StockChange request) {
        return R.ok(inventoryService.inbound(request));
    }

    @PostMapping("/outbound")
    public R<Stock> outbound(@Valid @RequestBody InventoryForms.StockChange request) {
        return R.ok(inventoryService.outbound(request));
    }

    @PostMapping("/stocktakes")
    public R<Stocktake> stocktake(@Valid @RequestBody InventoryForms.StocktakeCreate request) {
        return R.ok(inventoryService.stocktake(request));
    }

    @GetMapping("/stocks")
    public R<List<Stock>> stocks(@RequestParam Long storeId) {
        return R.ok(inventoryService.listStocks(storeId));
    }

    @GetMapping("/flows")
    public R<List<StockFlow>> flows(@RequestParam Long storeId, @RequestParam(required = false) Long productId) {
        return R.ok(inventoryService.listFlows(storeId, productId));
    }

    @GetMapping("/alerts")
    public R<List<LowStockRow>> alerts(@RequestParam Long storeId) {
        return R.ok(inventoryService.lowStock(storeId));
    }
}
