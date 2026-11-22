package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.BookingForms;

import java.util.List;
import com.zhangmei.booking.dto.BeauticianScore;
import com.zhangmei.booking.service.AccountService;
import com.zhangmei.booking.service.BeauticianScoreService;
import com.zhangmei.booking.service.AppointmentCommandService;
import com.zhangmei.booking.service.CatalogService;
import com.zhangmei.booking.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalCatalogController {

    private final CatalogService catalogService;
    private final AppointmentCommandService appointmentCommandService;
    private final AccountService accountService;
    private final MessageService messageService;
    private final BeauticianScoreService beauticianScoreService;

    @PostMapping("/stores")
    public R<Void> store(@Valid @RequestBody BookingForms.StoreSync request) {
        catalogService.syncStore(request);
        return R.ok(null);
    }

    @PostMapping("/service-items")
    public R<Void> service(@Valid @RequestBody BookingForms.ServiceSync request) {
        catalogService.syncService(request);
        return R.ok(null);
    }

    @PostMapping("/beauticians")
    public R<Void> beautician(@Valid @RequestBody BookingForms.BeauticianSync request) {
        catalogService.syncBeautician(request);
        return R.ok(null);
    }

    @PostMapping("/service-staff")
    public R<Void> syncServiceStaff(@Valid @RequestBody BookingForms.ServiceStaffSync request) {
        catalogService.syncServiceStaff(request);
        return R.ok(null);
    }

    @PostMapping("/beautician-services")
    public R<Void> bindings(@Valid @RequestBody BookingForms.BindSync request) {
        catalogService.syncBindings(request);
        return R.ok(null);
    }

    @PostMapping("/schedules")
    public R<Void> schedule(@Valid @RequestBody BookingForms.ScheduleSync request) {
        catalogService.syncSchedule(request);
        return R.ok(null);
    }

    @PostMapping("/members/level")
    public R<Integer> memberLevel(@Valid @RequestBody BookingForms.MemberLevelSync request) {
        return R.ok(accountService.syncMemberLevel(request.getMobile(), request.getLevel()));
    }

    @PostMapping("/campaigns/notify")
    public R<Integer> campaign(@Valid @RequestBody BookingForms.CampaignNotify request) {
        return R.ok(messageService.pushCampaign(request));
    }

    @GetMapping("/beauticians/scores")
    public R<List<BeauticianScore>> scores() {
        return R.ok(beauticianScoreService.all());
    }

    @PostMapping("/appointments/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        appointmentCommandService.complete(id);
        return R.ok(null);
    }
}
