package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.dto.TeamStatusRow;
import com.zhangmei.staff.entity.Employee;
import com.zhangmei.staff.entity.WorkNote;
import com.zhangmei.staff.mapper.EmployeeMapper;
import com.zhangmei.staff.mapper.TeamStatusMapper;
import com.zhangmei.staff.mapper.WorkNoteMapper;
import com.zhangmei.staff.security.StaffContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final Set<String> STATUSES = Set.of("ON_DUTY", "OFF_DUTY", "LEAVE", "LEFT");
    private static final Set<String> BIZ_TYPES = Set.of("ATTENDANCE", "ROUTE", "INSPECTION", "FOLLOWUP", "PERFORMANCE");

    private final TeamStatusMapper teamStatusMapper;
    private final EmployeeMapper employeeMapper;
    private final WorkNoteMapper workNoteMapper;

    public List<TeamStatusRow> status(String status) {
        if (status != null && !status.isBlank() && !STATUSES.contains(status)) {
            throw new BizException("工作状态无效");
        }
        return teamStatusMapper.listStatus(status, LocalDate.now());
    }

    public void changeStatus(Long employeeId, String status) {
        if (!STATUSES.contains(status)) {
            throw new BizException("工作状态无效");
        }
        Employee employee = mustEmployee(employeeId);
        employee.setStatus(status);
        employeeMapper.updateById(employee);
    }

    public Long annotate(StaffForms.WorkNoteCreate request) {
        if (request.getBizType() != null && !request.getBizType().isBlank() && !BIZ_TYPES.contains(request.getBizType())) {
            throw new BizException("批注业务类型无效");
        }
        mustEmployee(request.getTargetEmployeeId());
        WorkNote note = new WorkNote();
        note.setTargetEmployeeId(request.getTargetEmployeeId());
        note.setAuthorEmployeeId(StaffContext.requireEmployeeId());
        note.setBizType(request.getBizType());
        note.setBizId(request.getBizId());
        note.setContent(request.getContent().trim());
        workNoteMapper.insert(note);
        return note.getId();
    }

    public List<WorkNote> notes(Long targetEmployeeId) {
        mustEmployee(targetEmployeeId);
        return workNoteMapper.selectList(new LambdaQueryWrapper<WorkNote>()
                .eq(WorkNote::getTargetEmployeeId, targetEmployeeId)
                .orderByDesc(WorkNote::getId));
    }

    private Employee mustEmployee(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BizException(404, "员工不存在");
        }
        return employee;
    }
}
