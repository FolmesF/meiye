package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.BeauticianAvailability;
import com.zhangmei.booking.dto.SlotBoard;
import com.zhangmei.booking.redis.LoginTokenStore;
import com.zhangmei.booking.service.AvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final LoginTokenStore loginTokenStore;

    @GetMapping
    public R<List<BeauticianAvailability>> query(@RequestParam Long storeId,
                                                  @RequestParam Long serviceItemId,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(availabilityService.query(storeId, serviceItemId, date));
    }

    @GetMapping("/slots")
    public R<SlotBoard> slots(@RequestParam Long beauticianId,
                              @RequestParam Long serviceItemId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              HttpServletRequest request) {
        return R.ok(availabilityService.board(beauticianId, serviceItemId, date, currentUser(request)));
    }

    private Long currentUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return loginTokenStore.getUserId(header.substring(7).trim());
    }
}
