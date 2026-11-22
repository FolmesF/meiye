package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.common.UserContext;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.entity.BeauticianSnapshot;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.entity.BookingConsumption;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.entity.ScheduleSnapshot;
import com.zhangmei.booking.entity.ServiceSnapshot;
import com.zhangmei.booking.mapper.BookingAppointmentMapper;
import com.zhangmei.booking.mapper.BookingConsumptionMapper;
import com.zhangmei.booking.mapper.BookingUserMapper;
import com.zhangmei.booking.store.StoreClient;
import com.zhangmei.booking.notify.NotificationService;
import com.zhangmei.booking.notify.NotifyCommand;
import com.zhangmei.booking.redis.BeauticianLock;
import com.zhangmei.booking.redis.SlotCache;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentCommandService {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final List<String> MUTABLE = List.of("PENDING", "CONFIRMED");

    private final CatalogService catalogService;
    private final AvailabilityService availabilityService;
    private final BookingAppointmentMapper appointmentMapper;
    private final BookingConsumptionMapper consumptionMapper;
    private final BeauticianLock beauticianLock;
    private final SlotCache slotCache;
    private final NotificationService notificationService;
    private final BookingUserMapper userMapper;
    private final StoreClient storeClient;
    private final PlatformTransactionManager transactionManager;

    public BookingAppointment create(BookingForms.CreateAppointment request) {
        Long userId = UserContext.require();
        PreparedSlot slot = prepare(request.getBeauticianId(), request.getServiceItemId(), request.getAppointTime(), null);
        return withBeauticianLock(slot.beautician().getId(), () -> inTransaction(() -> {
            assertNoOverlap(slot.beautician().getId(), slot.start(), slot.end(), null);
            BookingAppointment appointment = new BookingAppointment();
            appointment.setUserId(userId);
            appointment.setStoreId(slot.service().getStoreId());
            appointment.setBeauticianId(slot.beautician().getId());
            appointment.setServiceItemId(slot.service().getId());
            appointment.setAppointTime(slot.start());
            appointment.setAppointEnd(slot.end());
            appointment.setStatus("PENDING");
            appointment.setAmount(slot.service().getPrice());
            appointment.setSlotLock(SlotCacheKeys.slotLockValue(slot.beautician().getId(), slot.start()));
            appointment.setReminded(0);
            try {
                appointmentMapper.insert(appointment);
            } catch (DuplicateKeyException exception) {
                throw new BizException(409, "该时段已被预约");
            }
            slotCache.bump(slot.beautician().getId(), slot.start().toLocalDate());
            notificationService.notifyBooking(message(userId, "BOOKING_SUCCESS", "预约成功",
                    slot.beautician().getName() + " " + slot.service().getName() + " " + CLOCK.format(slot.start()),
                    appointment.getId()));
            pushStore(appointment, slot.beautician(), slot.service(), "PENDING", null);
            return appointment;
        }));
    }

    public void cancel(Long appointmentId, String reason) {
        BookingAppointment appointment = requireOwned(appointmentId);
        if (!MUTABLE.contains(appointment.getStatus())) {
            throw new BizException("当前状态不能取消");
        }
        withBeauticianLock(appointment.getBeauticianId(), () -> inTransaction(() -> {
            int updated = appointmentMapper.update(null, new LambdaUpdateWrapper<BookingAppointment>()
                    .eq(BookingAppointment::getId, appointment.getId())
                    .in(BookingAppointment::getStatus, MUTABLE)
                    .set(BookingAppointment::getStatus, "CANCELLED")
                    .set(BookingAppointment::getSlotLock, null)
                    .set(BookingAppointment::getCancelReason, reason));
            if (updated != 1) {
                throw new BizException("取消失败，请刷新后重试");
            }
            slotCache.bump(appointment.getBeauticianId(), appointment.getAppointTime().toLocalDate());
            notificationService.notifyBooking(message(appointment.getUserId(), "BOOKING_CANCEL", "预约已取消",
                    "原预约时间 " + CLOCK.format(appointment.getAppointTime()), appointment.getId()));
            pushStore(appointment, "CANCELLED", reason);
            return null;
        }));
    }

    public void reschedule(Long appointmentId, LocalDateTime appointTime) {
        BookingAppointment appointment = requireOwned(appointmentId);
        if (!MUTABLE.contains(appointment.getStatus())) {
            throw new BizException("当前状态不能改期");
        }
        PreparedSlot slot = prepare(appointment.getBeauticianId(), appointment.getServiceItemId(), appointTime, appointment.getId());
        withBeauticianLock(appointment.getBeauticianId(), () -> inTransaction(() -> {
            assertNoOverlap(appointment.getBeauticianId(), slot.start(), slot.end(), appointment.getId());
            int updated;
            try {
                updated = appointmentMapper.update(null, new LambdaUpdateWrapper<BookingAppointment>()
                        .eq(BookingAppointment::getId, appointment.getId())
                        .in(BookingAppointment::getStatus, MUTABLE)
                        .set(BookingAppointment::getAppointTime, slot.start())
                        .set(BookingAppointment::getAppointEnd, slot.end())
                        .set(BookingAppointment::getSlotLock, SlotCacheKeys.slotLockValue(appointment.getBeauticianId(), slot.start()))
                        .set(BookingAppointment::getReminded, 0));
            } catch (DuplicateKeyException exception) {
                throw new BizException(409, "该时段已被预约");
            }
            if (updated != 1) {
                throw new BizException("改期失败，请刷新后重试");
            }
            slotCache.bump(appointment.getBeauticianId(), appointment.getAppointTime().toLocalDate());
            slotCache.bump(appointment.getBeauticianId(), slot.start().toLocalDate());
            appointment.setAppointTime(slot.start());
            appointment.setAppointEnd(slot.end());
            notificationService.notifyBooking(message(appointment.getUserId(), "BOOKING_RESCHEDULE", "预约已改期",
                    "新的预约时间 " + CLOCK.format(slot.start()), appointment.getId()));
            pushStore(appointment, slot.beautician(), slot.service(), appointment.getStatus(), null);
            return null;
        }));
    }

    public void complete(Long appointmentId) {
        BookingAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if ("COMPLETED".equals(appointment.getStatus())) {
            return;
        }
        if (!List.of("PENDING", "CONFIRMED", "ARRIVED").contains(appointment.getStatus())) {
            throw new BizException("当前状态不能完成服务");
        }
        inTransaction(() -> {
            appointmentMapper.update(null, new LambdaUpdateWrapper<BookingAppointment>()
                    .eq(BookingAppointment::getId, appointment.getId())
                    .set(BookingAppointment::getStatus, "COMPLETED"));
            Long exists = consumptionMapper.selectCount(new LambdaQueryWrapper<BookingConsumption>()
                    .eq(BookingConsumption::getAppointmentId, appointment.getId()));
            if (exists == 0) {
                BookingConsumption consumption = new BookingConsumption();
                consumption.setUserId(appointment.getUserId());
                consumption.setStoreId(appointment.getStoreId());
                consumption.setAppointmentId(appointment.getId());
                consumption.setBeauticianId(appointment.getBeauticianId());
                consumption.setServiceItemId(appointment.getServiceItemId());
                consumption.setAmount(appointment.getAmount());
                consumption.setConsumedAt(LocalDateTime.now());
                consumptionMapper.insert(consumption);
            }
            return null;
        });
    }

    private PreparedSlot prepare(Long beauticianId, Long serviceItemId, LocalDateTime appointTime, Long excludeId) {
        LocalDateTime start = appointTime.withSecond(0).withNano(0);
        if (!start.equals(appointTime) || !start.isAfter(LocalDateTime.now())) {
            throw new BizException("请选择将来的整分钟时段");
        }
        BeauticianSnapshot beautician = catalogService.requireActiveBeautician(beauticianId);
        ServiceSnapshot service = catalogService.requireEnabledService(beautician.getStoreId(), serviceItemId);
        catalogService.requireBinding(beauticianId, serviceItemId);
        LocalDate date = start.toLocalDate();
        ScheduleSnapshot schedule = catalogService.requireOnDuty(beauticianId, date);
        LocalTime time = start.toLocalTime();
        List<LocalTime> openSlots = availabilityService.computeOpenSlots(
                beauticianId, service, schedule, date, excludeId);
        if (!openSlots.contains(time)) {
            throw new BizException("该时段不可预约");
        }
        return new PreparedSlot(beautician, service, start, start.plusMinutes(service.getDurationMin()));
    }

    private void assertNoOverlap(Long beauticianId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (appointmentMapper.countOverlap(beauticianId, start, end, excludeId) > 0) {
            throw new BizException(409, "该时段与已有预约冲突");
        }
    }

    private BookingAppointment requireOwned(Long appointmentId) {
        BookingAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getUserId().equals(UserContext.require())) {
            throw new BizException(404, "预约不存在");
        }
        return appointment;
    }

    private <T> T withBeauticianLock(Long beauticianId, java.util.function.Supplier<T> action) {
        if (!beauticianLock.tryLock(beauticianId)) {
            throw new BizException("该美容师正在被预约，请稍后重试");
        }
        try {
            return action.get();
        } finally {
            beauticianLock.unlock(beauticianId);
        }
    }

    private <T> T inTransaction(java.util.function.Supplier<T> action) {
        return new TransactionTemplate(transactionManager).execute(status -> action.get());
    }

    private void pushStore(BookingAppointment appointment, String status, String reason) {
        BeauticianSnapshot beautician = catalogService.requireActiveBeautician(appointment.getBeauticianId());
        ServiceSnapshot service = catalogService.requireEnabledService(appointment.getStoreId(), appointment.getServiceItemId());
        pushStore(appointment, beautician, service, status, reason);
    }

    private void pushStore(BookingAppointment appointment, BeauticianSnapshot beautician, ServiceSnapshot service,
                           String status, String reason) {
        BookingUser user = userMapper.selectById(appointment.getUserId());
        if (user == null || user.getMobile() == null) {
            throw new BizException("预约用户不存在，无法同步到门店");
        }
        storeClient.syncAppointment(appointment, user, beautician, service, status, reason);
    }

    private NotifyCommand message(Long userId, String type, String title, String content, Long bizId) {
        NotifyCommand command = new NotifyCommand();
        command.setUserId(userId);
        command.setType(type);
        command.setTitle(title);
        command.setContent(content);
        command.setBizId(bizId);
        return command;
    }

    private record PreparedSlot(BeauticianSnapshot beautician, ServiceSnapshot service, LocalDateTime start, LocalDateTime end) {
    }
}
