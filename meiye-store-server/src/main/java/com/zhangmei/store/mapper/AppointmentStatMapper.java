package com.zhangmei.store.mapper;

import com.zhangmei.store.dto.AppointmentStatRow;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentStatMapper {

    List<AppointmentStatRow> aggregate(@Param("storeId") Long storeId,
                                       @Param("dimension") String dimension,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
