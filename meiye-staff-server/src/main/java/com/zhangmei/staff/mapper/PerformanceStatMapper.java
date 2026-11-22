package com.zhangmei.staff.mapper;

import com.zhangmei.staff.dto.PerformanceStatRow;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface PerformanceStatMapper {

    List<PerformanceStatRow> aggregate(@Param("employeeId") Long employeeId,
                                       @Param("storeId") Long storeId,
                                       @Param("dimension") String dimension,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("groupByPerson") boolean groupByPerson,
                                       @Param("groupByStore") boolean groupByStore);
}
