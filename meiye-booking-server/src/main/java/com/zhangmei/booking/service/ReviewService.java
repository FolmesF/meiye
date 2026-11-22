package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.common.UserContext;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.entity.Review;
import com.zhangmei.booking.mapper.BookingAppointmentMapper;
import com.zhangmei.booking.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BookingAppointmentMapper appointmentMapper;
    private final ReviewMapper reviewMapper;

    public Long create(BookingForms.ReviewCreate request) {
        BookingAppointment appointment = appointmentMapper.selectById(request.getAppointmentId());
        if (appointment == null || !appointment.getUserId().equals(UserContext.require())) {
            throw new BizException(404, "预约不存在");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new BizException("服务完成后才能评价");
        }
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getAppointmentId, appointment.getId()));
        if (exists > 0) {
            throw new BizException(409, "该预约已评价");
        }
        Review review = new Review();
        review.setUserId(appointment.getUserId());
        review.setAppointmentId(appointment.getId());
        review.setStoreId(appointment.getStoreId());
        review.setBeauticianId(appointment.getBeauticianId());
        review.setServiceItemId(appointment.getServiceItemId());
        review.setBeauticianScore(request.getBeauticianScore());
        review.setStoreScore(request.getStoreScore());
        review.setServiceScore(request.getServiceScore());
        review.setContent(request.getContent());
        reviewMapper.insert(review);
        return review.getId();
    }
}
