package com.zhangmei.booking.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final InAppNotifyChannel inAppNotifyChannel;
    private final SmsNotifyChannel smsNotifyChannel;
    private final PushNotifyChannel pushNotifyChannel;

    public void notifyBooking(NotifyCommand command) {
        inAppNotifyChannel.send(command);
        Runnable external = () -> {
            smsNotifyChannel.send(command);
            pushNotifyChannel.send(command);
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    external.run();
                }
            });
            return;
        }
        external.run();
    }
}
