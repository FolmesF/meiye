package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.mapper.BookingAppointmentMapper;
import com.zhangmei.booking.notify.NotificationService;
import com.zhangmei.booking.notify.NotifyCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ArrivalReminder {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final BookingAppointmentMapper appointmentMapper;
    private final NotificationService notificationService;
    private final BookingProperties properties;
    private final PlatformTransactionManager transactionManager;

    @Scheduled(fixedDelay = 60_000)
    public void remind() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusHours(properties.getArrivalRemindHours());
        appointmentMapper.selectList(new LambdaQueryWrapper<BookingAppointment>()
                        .in(BookingAppointment::getStatus, "PENDING", "CONFIRMED")
                        .eq(BookingAppointment::getReminded, 0)
                        .ge(BookingAppointment::getAppointTime, now)
                        .le(BookingAppointment::getAppointTime, until))
                .forEach(this::remindOne);
    }

    private void remindOne(BookingAppointment appointment) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            int updated = appointmentMapper.update(null, new LambdaUpdateWrapper<BookingAppointment>()
                    .eq(BookingAppointment::getId, appointment.getId())
                    .eq(BookingAppointment::getReminded, 0)
                    .set(BookingAppointment::getReminded, 1));
            if (updated != 1) {
                return;
            }
            NotifyCommand command = new NotifyCommand();
            command.setUserId(appointment.getUserId());
            command.setType("ARRIVAL_REMIND");
            command.setTitle("到店提醒");
            command.setContent("预约将于 " + CLOCK.format(appointment.getAppointTime()) + " 开始，请准时到店");
            command.setBizId(appointment.getId());
            notificationService.notifyBooking(command);
        });
    }
}
