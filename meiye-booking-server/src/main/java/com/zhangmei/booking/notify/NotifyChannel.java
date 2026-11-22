package com.zhangmei.booking.notify;

public interface NotifyChannel {

    String channel();

    void send(NotifyCommand command);
}
