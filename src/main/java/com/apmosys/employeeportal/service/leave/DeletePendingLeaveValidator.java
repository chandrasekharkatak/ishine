package com.apmosys.employeeportal.service.leave;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.Exception.LeaveApplicationException;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;

@Component
public class DeletePendingLeaveValidator {

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeMasterRepository leaveTypeMasterRepository;

    @Autowired
    private LeavePolicyMasterRepository leavePolicyMasterRepository;

    public DeletePendingLeaveValidationResult validateForDelete(LeaveDTO leaveDTO) {
        if (leaveDTO == null || leaveDTO.getLeaveId() == null) {
            throw new LeaveApplicationException("leaveId is required.");
        }
        if (leaveDTO.getLeaveTypeMasterId() == null) {
            throw new LeaveApplicationException("leaveTypeMasterId is required.");
        }

        EmployeeLeave leaveToBeDeleted = employeeLeaveRepository.findById(leaveDTO.getLeaveId())
                .orElseThrow(() -> new LeaveApplicationException("Leave Application Not Found."));

        Employee employee = employeeRepository.findByEmpId(leaveToBeDeleted.getEmpId());
        if (employee == null) {
            throw new LeaveApplicationException("Employee not found for leave application.");
        }

        Optional<LeaveTypeMaster> leaveTypeOpt = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
        if (!leaveTypeOpt.isPresent()) {
            throw new LeaveApplicationException("Invalid leave type.");
        }
        LeaveTypeMaster leaveType = leaveTypeOpt.get();

        Integer expirationPeriod = null;
        if ("CO".equalsIgnoreCase(leaveType.getLeaveTypeCode())) {
            LeavePolicyMaster leavePolicy = leavePolicyMasterRepository
                    .findByLeaveTypeMasterIdAndEmploymentStatus(leaveType.getLeaveTypeMasterId(), employee.getEmploymentstatus());
            if (leavePolicy != null && "Yes".equalsIgnoreCase(leavePolicy.getExpirationPeriod())) {
                expirationPeriod = leavePolicy.getExpirationPeriodValue();
            }
        }

        return new DeletePendingLeaveValidationResult(leaveToBeDeleted, employee, leaveType, expirationPeriod);
    }
}
