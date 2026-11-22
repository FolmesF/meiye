package com.zhangmei.booking.notify;

import com.zhangmei.booking.entity.InboxMessage;
import com.zhangmei.booking.mapper.InboxMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotifyChannel implements NotifyChannel {

    private final InboxMessageMapper inboxMessageMapper;

    @Override
    public String channel() {
        return "IN_APP";
    }

    @Override
    public void send(NotifyCommand command) {
        InboxMessage message = new InboxMessage();
        message.setUserId(command.getUserId());
        message.setMsgType(command.getType());
        message.setTitle(command.getTitle());
        message.setContent(command.getContent());
        message.setBizId(command.getBizId());
        message.setReadFlag(0);
        message.setChannel(channel());
        inboxMessageMapper.insert(message);
    }
}
