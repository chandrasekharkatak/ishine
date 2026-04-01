package com.apmosys.employeeportal.service.leave;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.Exception.LeaveApplicationException;
import com.apmosys.employeeportal.dto.LeaveDTO;

@Component
public class GetAllLeaveApplicationsByEmpIdValidator {

    public GetAllLeaveApplicationsByEmpIdValidationResult validateRequest(LeaveDTO leaveDTO) {
        if (leaveDTO == null || leaveDTO.getEmpId() == null) {
            throw new LeaveApplicationException("empId is required.");
        }
        return new GetAllLeaveApplicationsByEmpIdValidationResult(leaveDTO.getEmpId());
    }
}
