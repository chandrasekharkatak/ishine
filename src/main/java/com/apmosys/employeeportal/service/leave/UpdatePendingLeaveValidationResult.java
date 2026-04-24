package com.apmosys.employeeportal.service.leave;

import java.time.LocalDate;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;

/**
 * Bundled output of update-pending-leave pre-persist validation.
 */
public final class UpdatePendingLeaveValidationResult {

    private final EmployeeLeave leaveToBeUpdated;
    private final LeaveTypeMaster leaveType;
    private final EmployeeLeavesMap employeeLeavesMap;
    private final EmployeeDTO employeeContext;
    private final double difference;
    private final LocalDate oldFromDate;
    private final LocalDate oldToDate;

    public UpdatePendingLeaveValidationResult(
            EmployeeLeave leaveToBeUpdated,
            LeaveTypeMaster leaveType,
            EmployeeLeavesMap employeeLeavesMap,
            EmployeeDTO employeeContext,
            double difference,
            LocalDate oldFromDate,
            LocalDate oldToDate) {
        this.leaveToBeUpdated = leaveToBeUpdated;
        this.leaveType = leaveType;
        this.employeeLeavesMap = employeeLeavesMap;
        this.employeeContext = employeeContext;
        this.difference = difference;
        this.oldFromDate = oldFromDate;
        this.oldToDate = oldToDate;
    }

    public EmployeeLeave leaveToBeUpdated() {
        return leaveToBeUpdated;
    }

    public LeaveTypeMaster leaveType() {
        return leaveType;
    }

    public EmployeeLeavesMap employeeLeavesMap() {
        return employeeLeavesMap;
    }

    public EmployeeDTO employeeContext() {
        return employeeContext;
    }

    public double difference() {
        return difference;
    }

    public LocalDate oldFromDate() {
        return oldFromDate;
    }

    public LocalDate oldToDate() {
        return oldToDate;
    }
}
