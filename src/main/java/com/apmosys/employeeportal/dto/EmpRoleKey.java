package com.apmosys.employeeportal.dto;

import java.util.Objects;

/**
 * Composite key used throughout the ishineToPoEmpDetails pipeline.
 * An employee who serves the same PO with two different roles (e.g. Jr.Dev then Sr.Dev)
 * produces TWO distinct keys, and therefore TWO result objects in the final response.
 */
public class EmpRoleKey {

    private final Long empId;
    private final Long roleId;  // role_id from employee_team_mapping

    public EmpRoleKey(Long empId, Long roleId) {
        this.empId  = empId;
        this.roleId = roleId;
    }

    public Long getEmpId()  { return empId;  }
    public Long getRoleId() { return roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmpRoleKey)) return false;
        EmpRoleKey that = (EmpRoleKey) o;
        return Objects.equals(empId, that.empId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empId, roleId);
    }

    @Override
    public String toString() {
        return "EmpRoleKey{empId=" + empId + ", roleId=" + roleId + "}";
    }
}
