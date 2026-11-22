package com.zhangmei.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.InventoryForms;
import com.zhangmei.store.dto.InventorySummaryRow;
import com.zhangmei.store.dto.LowStockRow;
import com.zhangmei.store.entity.Product;
import com.zhangmei.store.entity.ServiceMaterial;
import com.zhangmei.store.entity.Stock;
import com.zhangmei.store.entity.StockFlow;
import com.zhangmei.store.entity.Stocktake;
import com.zhangmei.store.mapper.ProductMapper;
import com.zhangmei.store.mapper.ServiceMaterialMapper;
import com.zhangmei.store.mapper.StockFlowMapper;
import com.zhangmei.store.mapper.StockMapper;
import com.zhangmei.store.mapper.StocktakeMapper;
import com.zhangmei.store.service.Checks;
import com.zhangmei.store.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");

    private final ProductMapper productMapper;
    private final StockMapper stockMapper;
    private final StockFlowMapper stockFlowMapper;
    private final StocktakeMapper stocktakeMapper;
    private final ServiceMaterialMapper serviceMaterialMapper;

    @Override
    @Transactional
    public Long createProduct(InventoryForms.ProductSave request) {
        assertSkuFree(request.getStoreId(), request.getSkuCode(), null);
        Product product = new Product();
        product.setStoreId(request.getStoreId());
        product.setName(request.getName().trim());
        product.setSkuCode(request.getSkuCode().trim());
        product.setUnit(request.getUnit());
        product.setWarnQuantity(request.getWarnQuantity());
        product.setMaxQuantity(request.getWarnQuantity() * 5);
        product.setStatus("ENABLED");
        product.setRemark(request.getRemark());
        productMapper.insert(product);
        Stock stock = new Stock();
        stock.setStoreId(product.getStoreId());
        stock.setProductId(product.getId());
        stock.setQuantity(0);
        stockMapper.insert(stock);
        return product.getId();
    }

    @Override
    public void updateProduct(Long id, InventoryForms.ProductUpdate request) {
        Product product = mustProduct(id);
        if (!STATUSES.contains(request.getStatus())) {
            throw new BizException("产品状态只允许 ENABLED 或 DISABLED");
        }
        assertSkuFree(product.getStoreId(), request.getSkuCode(), id);
        product.setName(request.getName().trim());
        product.setSkuCode(request.getSkuCode().trim());
        product.setUnit(request.getUnit());
        product.setWarnQuantity(request.getWarnQuantity());
        product.setStatus(request.getStatus());
        product.setRemark(request.getRemark());
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        mustProduct(id);
        productMapper.deleteById(id);
    }

    @Override
    public PageResult<Product> pageProducts(Long storeId, String keyword, long page, long size) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStoreId, storeId)
                .and(StringUtils.hasText(keyword), w -> w.like(Product::getName, keyword).or().like(Product::getSkuCode, keyword))
                .orderByDesc(Product::getId);
        Page<Product> result = productMapper.selectPage(new Page<>(Checks.page(page), Checks.size(size)), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Override
    @Transactional
    public Stock inbound(InventoryForms.StockChange request) {
        Stock stock = mustStock(request.getStoreId(), request.getProductId());
        int before = stock.getQuantity();
        if (stockMapper.increase(stock.getId(), request.getQuantity()) != 1) {
            throw new BizException("入库失败");
        }
        writeFlow(stock, "IN", request.getQuantity(), before, before + request.getQuantity(), request.getRemark());
        stock.setQuantity(before + request.getQuantity());
        return stock;
    }

    @Override
    @Transactional
    public Stock outbound(InventoryForms.StockChange request) {
        Stock stock = mustStock(request.getStoreId(), request.getProductId());
        int before = stock.getQuantity();
        if (stockMapper.deduct(stock.getId(), request.getQuantity()) != 1) {
            throw new BizException("库存不足");
        }
        int after = before - request.getQuantity();
        writeFlow(stock, "OUT", -request.getQuantity(), before, after, request.getRemark());
        stock.setQuantity(after);
        return stock;
    }

    @Override
    @Transactional
    public Stocktake stocktake(InventoryForms.StocktakeCreate request) {
        Stock stock = mustStock(request.getStoreId(), request.getProductId());
        int book = stock.getQuantity();
        int actual = request.getActualQuantity();
        if (stockMapper.compareAndSet(stock.getId(), book, actual) != 1) {
            throw new BizException("库存已变化，请重新盘点");
        }
        Stocktake stocktake = new Stocktake();
        stocktake.setStoreId(stock.getStoreId());
        stocktake.setProductId(stock.getProductId());
        stocktake.setBookQty(book);
        stocktake.setActualQty(actual);
        stocktake.setDiffQty(actual - book);
        stocktake.setRemark(request.getRemark());
        stocktakeMapper.insert(stocktake);
        writeFlow(stock, "STOCKTAKE", actual - book, book, actual, request.getRemark(), stocktake.getId());
        return stocktake;
    }

    @Override
    public List<Stock> listStocks(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        return stockMapper.selectList(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getStoreId, storeId)
                .orderByAsc(Stock::getProductId));
    }

    @Override
    public List<StockFlow> listFlows(Long storeId, Long productId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        return stockFlowMapper.selectList(new LambdaQueryWrapper<StockFlow>()
                .eq(StockFlow::getStoreId, storeId)
                .eq(productId != null, StockFlow::getProductId, productId)
                .orderByDesc(StockFlow::getId));
    }

    @Override
    public List<InventorySummaryRow> summary(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        LocalDate today = LocalDate.now();
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStoreId, storeId)
                .orderByAsc(Product::getId));
        return products.stream().map(product -> {
            Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                    .eq(Stock::getStoreId, storeId)
                    .eq(Stock::getProductId, product.getId()));
            InventorySummaryRow row = new InventorySummaryRow();
            row.setProductId(product.getId());
            row.setName(product.getName());
            row.setSkuCode(product.getSkuCode());
            row.setMaxQuantity(product.getMaxQuantity() == null ? 0 : product.getMaxQuantity());
            row.setWarnQuantity(product.getWarnQuantity() == null ? 0 : product.getWarnQuantity());
            row.setQuantity(stock == null || stock.getQuantity() == null ? 0 : stock.getQuantity());
            boolean sameDay = stock != null && today.equals(stock.getConsumeDate());
            row.setTodayConsumed(sameDay && stock.getTodayConsumed() != null ? stock.getTodayConsumed() : 0);
            row.setTotalConsumed(stock == null || stock.getTotalConsumed() == null ? 0 : stock.getTotalConsumed());
            return row;
        }).toList();
    }

    @Override
    @Transactional
    public void consumeForService(Long storeId, Long serviceItemId, Long appointmentId) {
        List<ServiceMaterial> lines = serviceMaterialMapper.selectList(new LambdaQueryWrapper<ServiceMaterial>()
                .eq(ServiceMaterial::getServiceItemId, serviceItemId));
        for (ServiceMaterial line : lines) {
            if (line.getQuantity() == null || line.getQuantity().signum() <= 0) {
                continue;
            }
            int qty = line.getQuantity().setScale(0, RoundingMode.HALF_UP).intValue();
            if (qty <= 0) {
                continue;
            }
            Stock stock = mustStock(storeId, line.getProductId());
            int before = stock.getQuantity() == null ? 0 : stock.getQuantity();
            if (stockMapper.consume(stock.getId(), qty) != 1) {
                Product product = mustProduct(line.getProductId());
                throw new BizException(product.getName() + "库存不足，无法完成项目");
            }
            writeFlow(stock, "OUT", -qty, before, before - qty, "项目完成扣减", appointmentId);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(Long productId, InventoryForms.QuantityUpdate request) {
        Stock stock = mustStock(request.getStoreId(), productId);
        int before = stock.getQuantity() == null ? 0 : stock.getQuantity();
        int after = request.getQuantity();
        stock.setQuantity(after);
        stockMapper.updateById(stock);
        if (before != after) {
            writeFlow(stock, "STOCKTAKE", after - before, before, after, "修改剩余数量");
        }
    }

    @Override
    public void updateMaxQuantity(Long productId, int maxQuantity) {
        Product product = mustProduct(productId);
        product.setMaxQuantity(maxQuantity);
        product.setWarnQuantity(maxQuantity * 20 / 100);
        productMapper.updateById(product);
    }

    @Override
    public List<LowStockRow> lowStock(Long storeId) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        return productMapper.selectLowStock(storeId);
    }

    private void writeFlow(Stock stock, String bizType, int changeQty, int before, int after, String remark) {
        writeFlow(stock, bizType, changeQty, before, after, remark, null);
    }

    private void writeFlow(Stock stock, String bizType, int changeQty, int before, int after, String remark, Long bizId) {
        StockFlow flow = new StockFlow();
        flow.setStoreId(stock.getStoreId());
        flow.setProductId(stock.getProductId());
        flow.setBizType(bizType);
        flow.setChangeQty(changeQty);
        flow.setBeforeQty(before);
        flow.setAfterQty(after);
        flow.setBizId(bizId);
        flow.setRemark(remark);
        stockFlowMapper.insert(flow);
    }

    private Product mustProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "产品不存在");
        }
        return product;
    }

    private Stock mustStock(Long storeId, Long productId) {
        Product product = mustProduct(productId);
        if (!product.getStoreId().equals(storeId)) {
            throw new BizException("产品不属于该门店");
        }
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getStoreId, storeId)
                .eq(Stock::getProductId, productId));
        if (stock == null) {
            throw new BizException("库存记录不存在");
        }
        return stock;
    }

    private void assertSkuFree(Long storeId, String skuCode, Long excludeId) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getStoreId, storeId)
                .eq(Product::getSkuCode, skuCode.trim())
                .ne(excludeId != null, Product::getId, excludeId));
        if (count > 0) {
            throw new BizException(409, "该门店已存在相同 SKU");
        }
    }
}
