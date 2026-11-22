package com.zhangmei.staff.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.config.StaffProperties;
import com.zhangmei.staff.dto.LoginResult;
import com.zhangmei.staff.entity.AuthToken;
import com.zhangmei.staff.entity.Employee;
import com.zhangmei.staff.entity.EmployeeRole;
import com.zhangmei.staff.entity.Role;
import com.zhangmei.staff.mapper.AuthTokenMapper;
import com.zhangmei.staff.mapper.EmployeeMapper;
import com.zhangmei.staff.mapper.EmployeeRoleMapper;
import com.zhangmei.staff.mapper.PermissionMapper;
import com.zhangmei.staff.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeMapper employeeMapper;
    private final RoleMapper roleMapper;
    private final EmployeeRoleMapper employeeRoleMapper;
    private final PermissionMapper permissionMapper;
    private final AuthTokenMapper authTokenMapper;
    private final StaffProperties properties;

    public LoginResult login(String mobile) {
        Employee employee = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>().eq(Employee::getMobile, mobile));
        if (employee == null) {
            throw new BizException(404, "员工不存在");
        }
        if ("LEFT".equals(employee.getStatus())) {
            throw new BizException(403, "员工已离职");
        }
        AuthToken token = new AuthToken();
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setEmployeeId(employee.getId());
        token.setExpireAt(LocalDateTime.now().plusDays(properties.getTokenTtlDays()));
        token.setCreatedAt(LocalDateTime.now());
        authTokenMapper.insert(token);
        LoginResult result = new LoginResult();
        result.setToken(token.getToken());
        result.setEmployeeId(employee.getId());
        result.setName(employee.getName());
        result.setJobType(employee.getJobType());
        result.setPermissions(permissionCodes(employee.getId()));
        return result;
    }

    public Long resolveEmployee(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        AuthToken row = authTokenMapper.selectById(token);
        if (row == null || row.getExpireAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return row.getEmployeeId();
    }

    public Set<String> permissionCodes(Long employeeId) {
        return Set.copyOf(permissionMapper.selectCodesByEmployee(employeeId));
    }

    @Transactional
    public Long createEmployee(String name, String mobile, String roleCode, java.math.BigDecimal baseLongitude, java.math.BigDecimal baseLatitude) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role == null) {
            throw new BizException("角色不存在");
        }
        Long exists = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>().eq(Employee::getMobile, mobile));
        if (exists > 0) {
            throw new BizException(409, "手机号已存在");
        }
        if (baseLongitude != null || baseLatitude != null) {
            if (baseLongitude == null || baseLatitude == null) {
                throw new BizException("基准经纬度需要同时填写");
            }
            com.zhangmei.staff.common.GeoDistance.checkCoordinate(baseLongitude.doubleValue(), baseLatitude.doubleValue());
        }
        Employee employee = new Employee();
        employee.setName(name.trim());
        employee.setMobile(mobile);
        employee.setJobType(role.getCode());
        employee.setStatus("OFF_DUTY");
        employee.setBaseLongitude(baseLongitude);
        employee.setBaseLatitude(baseLatitude);
        employeeMapper.insert(employee);
        EmployeeRole link = new EmployeeRole();
        link.setEmployeeId(employee.getId());
        link.setRoleId(role.getId());
        link.setCreatedAt(LocalDateTime.now());
        employeeRoleMapper.insert(link);
        return employee.getId();
    }
}
