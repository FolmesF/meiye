package com.zhangmei.staff.mapper;

import com.zhangmei.staff.dto.TeamStatusRow;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface TeamStatusMapper {

    List<TeamStatusRow> listStatus(@Param("status") String status, @Param("today") LocalDate today);
}
