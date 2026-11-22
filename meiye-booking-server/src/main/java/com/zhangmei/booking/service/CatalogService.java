package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.entity.BeauticianServiceSnapshot;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.ScheduleSnapshot;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.entity.StoreSnapshot;
import com.zhangmei.booking.mapper.BeauticianServiceSnapshotMapper;
import com.zhangmei.booking.mapper.BeauticianSnapshotMapper;
import com.zhangmei.booking.mapper.ScheduleSnapshotMapper;
import com.zhangmei.booking.mapper.ServiceSnapshotMapper;
import com.zhangmei.booking.mapper.StoreSnapshotMapper;
import com.zhangmei.booking.redis.SlotCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final StoreSnapshotMapper storeMapper;
    private final ServiceSnapshotMapper serviceMapper;
    private final BeauticianSnapshotMapper beauticianMapper;
    private final BeauticianServiceSnapshotMapper bindMapper;
    private final ScheduleSnapshotMapper scheduleMapper;
    private final SlotCache slotCache;

    public ServiceSnapshot requireEnabledService(Long storeId, Long serviceItemId) {
        ServiceSnapshot item = serviceMapper.selectById(serviceItemId);
        if (item == null || !storeId.equals(item.getStoreId()) || !"ENABLED".equals(item.getStatus())) {
            throw new BizException("服务项目不可预约");
        }
        return item;
    }

    public BeauticianSnapshot requireActiveBeautician(Long beauticianId) {
        BeauticianSnapshot beautician = beauticianMapper.selectById(beauticianId);
        if (beautician == null || !"ACTIVE".equals(beautician.getStatus())) {
            throw new BizException("美容师不可预约");
        }
        return beautician;
    }

    public void requireBinding(Long beauticianId, Long serviceItemId) {
        Long count = bindMapper.selectCount(new LambdaQueryWrapper<BeauticianServiceSnapshot>()
                .eq(BeauticianServiceSnapshot::getBeauticianId, beauticianId)
                .eq(BeauticianServiceSnapshot::getServiceItemId, serviceItemId));
        if (count == 0) {
            throw new BizException("该美容师不提供此项目");
        }
    }

    public ScheduleSnapshot requireOnDuty(Long beauticianId, LocalDate date) {
        ScheduleSnapshot schedule = findSchedule(beauticianId, date);
        if (schedule == null || !"ON_DUTY".equals(schedule.getShiftStatus())
                || schedule.getShiftStart() == null || schedule.getShiftEnd() == null) {
            throw new BizException("美容师当天没有可约班次");
        }
        return schedule;
    }

    public ScheduleSnapshot findSchedule(Long beauticianId, LocalDate date) {
        return scheduleMapper.selectOne(new LambdaQueryWrapper<ScheduleSnapshot>()
                .eq(ScheduleSnapshot::getBeauticianId, beauticianId)
                .eq(ScheduleSnapshot::getWorkDate, date));
    }

    public List<BeauticianSnapshot> listBoundBeauticians(Long storeId, Long serviceItemId) {
        List<Long> beauticianIds = bindMapper.selectList(new LambdaQueryWrapper<BeauticianServiceSnapshot>()
                        .eq(BeauticianServiceSnapshot::getServiceItemId, serviceItemId))
                .stream()
                .map(BeauticianServiceSnapshot::getBeauticianId)
                .toList();
        if (beauticianIds.isEmpty()) {
            return List.of();
        }
        return beauticianMapper.selectList(new LambdaQueryWrapper<BeauticianSnapshot>()
                .eq(BeauticianSnapshot::getStoreId, storeId)
                .eq(BeauticianSnapshot::getStatus, "ACTIVE")
                .in(BeauticianSnapshot::getId, beauticianIds)
                .orderByAsc(BeauticianSnapshot::getId));
    }

    @Transactional
    public void syncStore(BookingForms.StoreSync request) {
        StoreSnapshot row = new StoreSnapshot();
        row.setId(request.getId());
        row.setName(request.getName());
        row.setPhone(request.getPhone());
        row.setAddress(request.getAddress());
        row.setStatus(request.getStatus());
        upsert(storeMapper.selectById(request.getId()), row, storeMapper::insert, storeMapper::updateById);
    }

    @Transactional
    public void syncService(BookingForms.ServiceSync request) {
        ServiceSnapshot row = new ServiceSnapshot();
        row.setId(request.getId());
        row.setStoreId(request.getStoreId());
        row.setName(request.getName());
        row.setPrice(request.getPrice());
        row.setDurationMin(request.getDurationMin());
        row.setStatus(request.getStatus());
        upsert(serviceMapper.selectById(request.getId()), row, serviceMapper::insert, serviceMapper::updateById);
    }

    @Transactional
    public void syncBeautician(BookingForms.BeauticianSync request) {
        BeauticianSnapshot row = new BeauticianSnapshot();
        row.setId(request.getId());
        row.setStoreId(request.getStoreId());
        row.setName(request.getName());
        row.setLevel(request.getLevel());
        row.setAvatar(request.getAvatar());
        row.setEntryDate(request.getEntryDate());
        row.setStatus(request.getStatus());
        upsert(beauticianMapper.selectById(request.getId()), row, beauticianMapper::insert, beauticianMapper::updateById);
    }

    @Transactional
    public void syncServiceStaff(BookingForms.ServiceStaffSync request) {
        syncService(request.getService());
        for (BookingForms.BeauticianSync beautician : request.getBeauticians()) {
            syncBeautician(beautician);
            ensureComingWeek(beautician.getStoreId(), beautician.getId());
        }
        bindMapper.delete(new LambdaQueryWrapper<BeauticianServiceSnapshot>()
                .eq(BeauticianServiceSnapshot::getServiceItemId, request.getService().getId()));
        for (Long beauticianId : request.getBeauticianIds()) {
            BeauticianServiceSnapshot row = new BeauticianServiceSnapshot();
            row.setBeauticianId(beauticianId);
            row.setServiceItemId(request.getService().getId());
            bindMapper.insert(row);
        }
        LocalDate today = LocalDate.now();
        for (Long beauticianId : request.getBeauticianIds()) {
            for (int offset = 0; offset < 7; offset++) {
                slotCache.bump(beauticianId, today.plusDays(offset));
            }
        }
    }

    private void ensureComingWeek(Long storeId, Long beauticianId) {
        LocalDate today = LocalDate.now();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = today.plusDays(offset);
            if (findSchedule(beauticianId, date) != null) {
                continue;
            }
            ScheduleSnapshot row = new ScheduleSnapshot();
            row.setId(IdWorker.getId());
            row.setStoreId(storeId);
            row.setBeauticianId(beauticianId);
            row.setWorkDate(date);
            row.setShiftStatus("ON_DUTY");
            row.setShiftStart(LocalTime.of(9, 30));
            row.setShiftEnd(LocalTime.of(21, 0));
            scheduleMapper.insert(row);
        }
    }

    @Transactional
    public void syncBindings(BookingForms.BindSync request) {
        bindMapper.delete(new LambdaQueryWrapper<BeauticianServiceSnapshot>()
                .eq(BeauticianServiceSnapshot::getBeauticianId, request.getBeauticianId()));
        for (Long serviceItemId : request.getServiceItemIds()) {
            BeauticianServiceSnapshot bind = new BeauticianServiceSnapshot();
            bind.setBeauticianId(request.getBeauticianId());
            bind.setServiceItemId(serviceItemId);
            bindMapper.insert(bind);
        }
    }

    @Transactional
    public void syncSchedule(BookingForms.ScheduleSync request) {
        ScheduleSnapshot row = new ScheduleSnapshot();
        row.setId(request.getId());
        row.setStoreId(request.getStoreId());
        row.setBeauticianId(request.getBeauticianId());
        row.setWorkDate(request.getWorkDate());
        row.setShiftStatus(request.getShiftStatus());
        row.setShiftStart(request.getShiftStart());
        row.setShiftEnd(request.getShiftEnd());
        upsert(scheduleMapper.selectById(request.getId()), row, scheduleMapper::insert, scheduleMapper::updateById);
        slotCache.bump(request.getBeauticianId(), request.getWorkDate());
    }

    private <T> void upsert(T existing, T row, java.util.function.Consumer<T> insert, java.util.function.Consumer<T> update) {
        if (existing == null) {
            insert.accept(row);
        } else {
            update.accept(row);
        }
    }
}
