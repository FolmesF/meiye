package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.dto.BeauticianAvailability;
import com.zhangmei.booking.dto.SlotBoard;
import com.zhangmei.booking.dto.SlotCell;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.entity.ScheduleSnapshot;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.mapper.BookingAppointmentMapper;
import com.zhangmei.booking.redis.SlotCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final CatalogService catalogService;
    private final BookingAppointmentMapper appointmentMapper;
    private final SlotCache slotCache;
    private final BookingProperties properties;

    public List<BeauticianAvailability> query(Long storeId, Long serviceItemId, LocalDate date) {
        ServiceSnapshot service = catalogService.requireEnabledService(storeId, serviceItemId);
        return catalogService.listBoundBeauticians(storeId, serviceItemId).stream()
                .map(beautician -> toAvailability(beautician, service, date))
                .filter(item -> item != null)
                .toList();
    }

    public List<LocalTime> computeOpenSlots(Long beauticianId, ServiceSnapshot service, ScheduleSnapshot schedule, LocalDate date) {
        return computeOpenSlots(beauticianId, service, schedule, date, null);
    }

    public List<LocalTime> computeOpenSlots(Long beauticianId, ServiceSnapshot service, ScheduleSnapshot schedule,
                                            LocalDate date, Long excludeAppointmentId) {
        List<SlotPlanner.TimeRange> occupied = appointmentMapper.selectList(new LambdaQueryWrapper<BookingAppointment>()
                        .eq(BookingAppointment::getBeauticianId, beauticianId)
                        .in(BookingAppointment::getStatus, "PENDING", "CONFIRMED", "ARRIVED", "COMPLETED")
                        .lt(BookingAppointment::getAppointTime, date.plusDays(1).atStartOfDay())
                        .gt(BookingAppointment::getAppointEnd, date.atStartOfDay())
                        .ne(excludeAppointmentId != null, BookingAppointment::getId, excludeAppointmentId))
                .stream()
                .map(item -> new SlotPlanner.TimeRange(item.getAppointTime().toLocalTime(), item.getAppointEnd().toLocalTime()))
                .toList();
        return SlotPlanner.available(date, schedule.getShiftStart(), schedule.getShiftEnd(),
                service.getDurationMin(), properties.getSlotStepMinutes(), occupied);
    }

    private BeauticianAvailability toAvailability(BeauticianSnapshot beautician, ServiceSnapshot service, LocalDate date) {
        ScheduleSnapshot schedule = catalogService.findSchedule(beautician.getId(), date);
        if (schedule == null || !"ON_DUTY".equals(schedule.getShiftStatus())) {
            return null;
        }
        List<LocalTime> slots = openSlots(beautician.getId(), service, date);
        BeauticianAvailability availability = new BeauticianAvailability();
        availability.setBeauticianId(beautician.getId());
        availability.setName(beautician.getName());
        availability.setLevel(beautician.getLevel());
        availability.setAvatar(beautician.getAvatar());
        availability.setSlots(slots.stream().map(time -> time.format(CLOCK)).toList());
        return availability;
    }

    /** 读取 Redis 可约缓存；未命中时按班次和已占用时段计算并回填。 */
    public List<LocalTime> openSlots(Long beauticianId, ServiceSnapshot service, LocalDate date) {
        ScheduleSnapshot schedule = catalogService.findSchedule(beauticianId, date);
        if (schedule == null || !"ON_DUTY".equals(schedule.getShiftStatus())) {
            return List.of();
        }
        return slotCache.get(beauticianId, service.getId(), date)
                .orElseGet(() -> {
                    List<LocalTime> computed = computeOpenSlots(beauticianId, service, schedule, date);
                    slotCache.put(beauticianId, service.getId(), date, computed);
                    return computed;
                });
    }

    public SlotBoard board(Long beauticianId, Long serviceItemId, LocalDate date, Long userId) {
        BeauticianSnapshot beautician = catalogService.requireActiveBeautician(beauticianId);
        ServiceSnapshot service = catalogService.requireEnabledService(beautician.getStoreId(), serviceItemId);
        catalogService.requireBinding(beauticianId, serviceItemId);
        ScheduleSnapshot schedule = catalogService.findSchedule(beauticianId, date);
        SlotBoard board = new SlotBoard();
        board.setBeauticianName(beautician.getName());
        board.setServiceName(service.getName());
        board.setPrice(service.getPrice());
        board.setDurationMin(service.getDurationMin());
        if (schedule == null || !"ON_DUTY".equals(schedule.getShiftStatus())
                || schedule.getShiftStart() == null || schedule.getShiftEnd() == null) {
            board.setOnDuty(false);
            return board;
        }
        board.setOnDuty(true);
        Set<String> open = openSlots(beauticianId, service, date).stream()
                .map(time -> time.format(CLOCK))
                .collect(Collectors.toSet());
        List<BookingAppointment> occupied = appointmentMapper.selectList(new LambdaQueryWrapper<BookingAppointment>()
                .eq(BookingAppointment::getBeauticianId, beauticianId)
                .in(BookingAppointment::getStatus, "PENDING", "CONFIRMED", "ARRIVED", "COMPLETED")
                .lt(BookingAppointment::getAppointTime, date.plusDays(1).atStartOfDay())
                .gt(BookingAppointment::getAppointEnd, date.atStartOfDay()));
        LocalDateTime now = LocalDateTime.now();
        List<LocalTime> all = SlotPlanner.available(date, schedule.getShiftStart(), schedule.getShiftEnd(),
                service.getDurationMin(), properties.getSlotStepMinutes(), List.of());
        List<SlotCell> cells = new ArrayList<>();
        for (LocalTime time : all) {
            LocalDateTime start = LocalDateTime.of(date, time);
            LocalDateTime end = start.plusMinutes(service.getDurationMin());
            SlotCell cell = new SlotCell();
            cell.setStart(time.format(CLOCK));
            if (!start.isAfter(now)) {
                cell.setStatus("FULL");
            } else if (open.contains(cell.getStart())) {
                cell.setStatus("OPEN");
            } else if (userId != null && occupied.stream().anyMatch(item -> userId.equals(item.getUserId())
                    && item.getAppointTime().isBefore(end) && item.getAppointEnd().isAfter(start))) {
                cell.setStatus("MINE");
            } else {
                cell.setStatus("FULL");
            }
            cells.add(cell);
        }
        board.setSlots(cells);
        return board;
    }
}
