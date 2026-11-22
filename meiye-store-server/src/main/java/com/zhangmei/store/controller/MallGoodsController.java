package com.zhangmei.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.entity.MallGoods;
import com.zhangmei.store.mapper.MallGoodsMapper;
import com.zhangmei.store.service.Checks;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mall/goods")
@RequiredArgsConstructor
public class MallGoodsController {

    private final MallGoodsMapper mallGoodsMapper;

    @GetMapping
    public R<PageResult<MallGoods>> page(@RequestParam Long storeId,
                                          @RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        Page<MallGoods> result = mallGoodsMapper.selectPage(
                new Page<>(Checks.page(page), Checks.size(size)),
                new LambdaQueryWrapper<MallGoods>()
                        .eq(MallGoods::getStoreId, storeId)
                        .orderByDesc(MallGoods::getId));
        return R.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }
}
