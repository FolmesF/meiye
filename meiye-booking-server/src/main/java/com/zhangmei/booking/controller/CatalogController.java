package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.BeauticianCard;
import com.zhangmei.booking.dto.BeauticianDetail;
import com.zhangmei.booking.dto.ReviewItem;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.entity.StoreSnapshot;
import com.zhangmei.booking.service.BrowseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final BrowseService browseService;

    @GetMapping("/stores")
    public R<List<StoreSnapshot>> stores() {
        return R.ok(browseService.stores());
    }

    @GetMapping("/services")
    public R<List<ServiceSnapshot>> services(@RequestParam Long storeId) {
        return R.ok(browseService.services(storeId));
    }

    @GetMapping("/beauticians")
    public R<List<BeauticianCard>> beauticians(@RequestParam Long storeId,
                                                @RequestParam Long serviceItemId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(browseService.beauticians(storeId, serviceItemId, date));
    }

    @GetMapping("/beauticians/{id}")
    public R<BeauticianDetail> detail(@PathVariable Long id) {
        return R.ok(browseService.detail(id));
    }

    @GetMapping("/beauticians/{id}/reviews")
    public R<List<ReviewItem>> reviews(@PathVariable Long id) {
        return R.ok(browseService.reviews(id));
    }
}
