package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Attendance;
import com.zhangmei.staff.entity.Employee;
import com.zhangmei.staff.entity.RouteReport;
import com.zhangmei.staff.mapper.AttendanceMapper;
import com.zhangmei.staff.mapper.EmployeeMapper;
import com.zhangmei.staff.mapper.RouteReportMapper;
import com.zhangmei.staff.security.StaffContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldWorkService {

    private final EmployeeMapper employeeMapper;
    private final AttendanceMapper attendanceMapper;
    private final RouteReportMapper routeReportMapper;
    private final LocationGuard locationGuard;

    @Transactional
    public Attendance clock(StaffForms.Clock request) {
        Employee employee = requireActiveEmployee();
        locationGuard.assertNear(employee.getBaseLongitude(), employee.getBaseLatitude(), request.getLongitude(), request.getLatitude());
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employee.getId())
                .eq(Attendance::getWorkDate, today));
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEmployeeId(employee.getId());
            attendance.setWorkDate(today);
            attendance.setClockInAt(LocalDateTime.now());
            attendance.setClockInLng(request.getLongitude());
            attendance.setClockInLat(request.getLatitude());
            try {
                attendanceMapper.insert(attendance);
            } catch (DuplicateKeyException exception) {
                throw new BizException("请勿重复打卡");
            }
            employee.setStatus("ON_DUTY");
            employeeMapper.updateById(employee);
            return attendance;
        }
        if (attendance.getClockOutAt() != null) {
            throw new BizException("今日已完成上下班打卡");
        }
        attendance.setClockOutAt(LocalDateTime.now());
        attendance.setClockOutLng(request.getLongitude());
        attendance.setClockOutLat(request.getLatitude());
        attendanceMapper.updateById(attendance);
        employee.setStatus("OFF_DUTY");
        employeeMapper.updateById(employee);
        return attendance;
    }

    public List<Attendance> myAttendance(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BizException("考勤查询日期无效");
        }
        return attendanceMapper.selectList(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, StaffContext.requireEmployeeId())
                .ge(Attendance::getWorkDate, startDate)
                .le(Attendance::getWorkDate, endDate)
                .orderByDesc(Attendance::getWorkDate));
    }

    public Long reportRoute(StaffForms.RouteReportCreate request) {
        Employee employee = requireActiveEmployee();
        locationGuard.assertNear(employee.getBaseLongitude(), employee.getBaseLatitude(), request.getLongitude(), request.getLatitude());
        RouteReport report = new RouteReport();
        report.setEmployeeId(employee.getId());
        report.setLongitude(request.getLongitude());
        report.setLatitude(request.getLatitude());
        report.setAddress(request.getAddress());
        report.setContent(request.getContent().trim());
        report.setReportAt(LocalDateTime.now());
        routeReportMapper.insert(report);
        return report.getId();
    }

    public List<RouteReport> myRoutes(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BizException("报岗查询日期无效");
        }
        return routeReportMapper.selectList(new LambdaQueryWrapper<RouteReport>()
                .eq(RouteReport::getEmployeeId, StaffContext.requireEmployeeId())
                .ge(RouteReport::getReportAt, startDate.atStartOfDay())
                .lt(RouteReport::getReportAt, endDate.plusDays(1).atStartOfDay())
                .orderByDesc(RouteReport::getReportAt));
    }

    private Employee requireActiveEmployee() {
        Employee employee = employeeMapper.selectById(StaffContext.requireEmployeeId());
        if (employee == null) {
            throw new BizException(401, "请先登录");
        }
        if ("LEFT".equals(employee.getStatus()) || "LEAVE".equals(employee.getStatus())) {
            throw new BizException("当前状态不能打卡或报岗");
        }
        return employee;
    }
}
