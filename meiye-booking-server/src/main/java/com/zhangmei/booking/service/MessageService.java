package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.common.PageResult;
import com.zhangmei.booking.common.UserContext;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.entity.InboxMessage;
import com.zhangmei.booking.mapper.BookingUserMapper;
import com.zhangmei.booking.mapper.InboxMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InboxMessageMapper messageMapper;
    private final BookingUserMapper userMapper;

    public PageResult<InboxMessage> page(long page, long size, String msgType) {
        long current = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 20 : Math.min(size, 100);
        LambdaQueryWrapper<InboxMessage> wrapper = new LambdaQueryWrapper<InboxMessage>()
                .eq(InboxMessage::getUserId, UserContext.require())
                .orderByDesc(InboxMessage::getId);
        if (msgType != null && !msgType.isBlank()) {
            wrapper.eq(InboxMessage::getMsgType, msgType.trim());
        }
        Page<InboxMessage> result = messageMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public int pushCampaign(BookingForms.CampaignNotify request) {
        String content = campaignContent(request);
        List<BookingUser> users = userMapper.selectList(new LambdaQueryWrapper<BookingUser>()
                .eq(BookingUser::getStatus, "ACTIVE"));
        int created = 0;
        for (BookingUser user : users) {
            Long exists = messageMapper.selectCount(new LambdaQueryWrapper<InboxMessage>()
                    .eq(InboxMessage::getUserId, user.getId())
                    .eq(InboxMessage::getMsgType, "CAMPAIGN")
                    .eq(InboxMessage::getBizId, request.getCampaignId()));
            if (exists > 0) {
                continue;
            }
            InboxMessage message = new InboxMessage();
            message.setUserId(user.getId());
            message.setMsgType("CAMPAIGN");
            message.setTitle(request.getName());
            message.setContent(content);
            message.setBizId(request.getCampaignId());
            message.setReadFlag(0);
            message.setChannel("IN_APP");
            messageMapper.insert(message);
            created++;
        }
        return created;
    }

    private String campaignContent(BookingForms.CampaignNotify request) {
        String rule = request.getRuleText() == null || request.getRuleText().isBlank() ? "" : request.getRuleText().trim();
        String content = request.getCampaignType()
                + "，" + CLOCK.format(request.getStartTime())
                + " 至 " + CLOCK.format(request.getEndTime())
                + (rule.isEmpty() ? "" : "。" + rule);
        return content.length() > 500 ? content.substring(0, 500) : content;
    }

    public void markRead(Long id) {
        int updated = messageMapper.update(null, new LambdaUpdateWrapper<InboxMessage>()
                .eq(InboxMessage::getId, id)
                .eq(InboxMessage::getUserId, UserContext.require())
                .set(InboxMessage::getReadFlag, 1));
        if (updated != 1) {
            throw new BizException(404, "消息不存在");
        }
    }
}
