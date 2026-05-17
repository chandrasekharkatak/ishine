package com.apmosys.employeeportal.service.leave;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.LeaveTypeMaster;

/**
 * Bundled output of delete-pending-leave pre-delete validation.
 */
public final class DeletePendingLeaveValidationResult {

    private final EmployeeLeave leaveToBeDeleted;
    private final Employee employee;
    private final LeaveTypeMaster leaveType;
    private final Integer compOffExpirationPeriod;

    public DeletePendingLeaveValidationResult(
            EmployeeLeave leaveToBeDeleted,
            Employee employee,
            LeaveTypeMaster leaveType,
            Integer compOffExpirationPeriod) {
        this.leaveToBeDeleted = leaveToBeDeleted;
        this.employee = employee;
        this.leaveType = leaveType;
        this.compOffExpirationPeriod = compOffExpirationPeriod;
    }

    public EmployeeLeave leaveToBeDeleted() {
        return leaveToBeDeleted;
    }

    public Employee employee() {
        return employee;
    }

    public LeaveTypeMaster leaveType() {
        return leaveType;
    }

    public Integer compOffExpirationPeriod() {
        return compOffExpirationPeriod;
    }
}
