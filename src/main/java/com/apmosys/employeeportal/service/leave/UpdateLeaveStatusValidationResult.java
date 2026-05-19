package com.apmosys.employeeportal.service.leave;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;

/**
 * Bundled output of update-leave-status pre-validation.
 */
public final class UpdateLeaveStatusValidationResult {

    private final EmployeeLeave leaveApplication;
    private final EmployeeLeavesMap employeeLeavesMap;
    private final Employee employee;

    public UpdateLeaveStatusValidationResult(
            EmployeeLeave leaveApplication,
            EmployeeLeavesMap employeeLeavesMap,
            Employee employee) {
        this.leaveApplication = leaveApplication;
        this.employeeLeavesMap = employeeLeavesMap;
        this.employee = employee;
    }

    public EmployeeLeave leaveApplication() {
        return leaveApplication;
    }

    public EmployeeLeavesMap employeeLeavesMap() {
        return employeeLeavesMap;
    }

    public Employee employee() {
        return employee;
    }
}
