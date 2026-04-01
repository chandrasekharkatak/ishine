package com.apmosys.employeeportal.service.leave;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;

public final class LeaveMapAndType {

    public final EmployeeLeavesMap employeeLeavesMap;
    public final LeaveTypeMaster leaveType;

    public LeaveMapAndType(EmployeeLeavesMap employeeLeavesMap, LeaveTypeMaster leaveType) {
        this.employeeLeavesMap = employeeLeavesMap;
        this.leaveType = leaveType;
    }
}
