package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.PageResult;
import com.zhangmei.booking.common.R;
import com.zhangmei.booking.entity.InboxMessage;
import com.zhangmei.booking.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public R<PageResult<InboxMessage>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) String msgType) {
        return R.ok(messageService.page(page, size, msgType));
    }

    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return R.ok(null);
    }
}
