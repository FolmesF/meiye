package com.zhangmei.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.booking.entity.BookingAppointment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

public interface BookingAppointmentMapper extends BaseMapper<BookingAppointment> {

    @Select("""
            SELECT COUNT(*)
            FROM bk_appointment
            WHERE deleted = 0
              AND beautician_id = #{beauticianId}
              AND status IN ('PENDING', 'CONFIRMED', 'ARRIVED', 'COMPLETED')
              AND appoint_time < #{end}
              AND appoint_end > #{start}
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            """)
    int countOverlap(@Param("beauticianId") Long beauticianId,
                     @Param("start") LocalDateTime start,
                     @Param("end") LocalDateTime end,
                     @Param("excludeId") Long excludeId);
}
