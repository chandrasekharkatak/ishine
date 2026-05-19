package com.apmosys.employeeportal.service.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.Exception.LeaveApplicationException;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;

@Component
public class UpdateLeaveStatusValidator {

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private EmployeeLeavesMapRepository employeeLeavesMapRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public UpdateLeaveStatusValidationResult validateForStatusUpdate(LeaveDTO leaveDTO) {
        if (leaveDTO == null || leaveDTO.getLeaveId() == null) {
            throw new LeaveApplicationException("leaveId is required.");
        }
        if (leaveDTO.getLeaveEmpId() == null || leaveDTO.getLeaveTypeMasterId() == null) {
            throw new LeaveApplicationException("leaveEmpId and leaveTypeMasterId are required.");
        }
        if (leaveDTO.getEmpId() == null) {
            throw new LeaveApplicationException("empId is required.");
        }
        if (leaveDTO.getLeaveStatusId() == null) {
            throw new LeaveApplicationException("leaveStatusId is required.");
        }

        EmployeeLeave leaveApplication = employeeLeaveRepository.findById(leaveDTO.getLeaveId())
                .orElseThrow(() -> new LeaveApplicationException("No Leave Application found."));

        EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
                .findByEmpIdAndLeaveTypeMasterId(leaveDTO.getLeaveEmpId(), leaveDTO.getLeaveTypeMasterId());
        if (employeeLeavesMap == null) {
            throw new LeaveApplicationException("Leave balance not found for employee and leave type.");
        }

        Employee employee = employeeRepository.findById(leaveDTO.getEmpId())
                .orElseThrow(() -> new LeaveApplicationException("Employee not found."));

        return new UpdateLeaveStatusValidationResult(leaveApplication, employeeLeavesMap, employee);
    }
}
