package com.zhangmei.staff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangmei.staff.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("""
            SELECT DISTINCT p.code
            FROM hr_employee_role er
            JOIN hr_role_permission rp ON rp.role_id = er.role_id
            JOIN hr_permission p ON p.id = rp.permission_id
            WHERE er.employee_id = #{employeeId}
            """)
    List<String> selectCodesByEmployee(@Param("employeeId") Long employeeId);
}
