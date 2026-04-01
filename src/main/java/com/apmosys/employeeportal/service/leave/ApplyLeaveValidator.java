package com.apmosys.employeeportal.service.leave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.DayTypeMasterNew;

/**
 * Orchestrates apply-leave validation by delegating to {@link LeaveApplicationValidator} in a fixed order.
 */
@Component
public class ApplyLeaveValidator {

    @Autowired
    private LeaveApplicationValidator leaveApplicationValidator;

    /**
     * Runs all pre-persist checks for apply leave. Throws {@link com.apmosys.employeeportal.Exception.LeaveApplicationException} on failure.
     */
    public ApplyLeaveValidationResult validateForApply(LeaveDTO dto) {
        leaveApplicationValidator.validateRequestPresent(dto);
        String logPayload = buildApplyLeaveLogPayload(dto);
        ParsedLeaveDates dates = leaveApplicationValidator.parseAndValidateDates(dto);
        leaveApplicationValidator.validateAuthorizedToApply(dto, logPayload);
        leaveApplicationValidator.validateNoOverlappingLeave(
                dto.getEmpId(), dates.fromDate, dates.toDate, logPayload);
        LeaveMapAndType leaveMapAndType = leaveApplicationValidator.loadLeaveMapAndType(dto);
        leaveApplicationValidator.validateCompOffBalanceIfApplicable(dto);
        leaveApplicationValidator.validateSufficientBalanceUnlessLwp(dto, leaveMapAndType.employeeLeavesMap);
        leaveApplicationValidator.validateCasualLeaveRules(dto);
        DayTypeMasterNew leaveDayType = leaveApplicationValidator.requireLeaveDayTypeForTimesheet();
        return new ApplyLeaveValidationResult(dates, leaveMapAndType, leaveDayType, logPayload);
    }

    private static String buildApplyLeaveLogPayload(LeaveDTO leaveDTO) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("empId : ").append(leaveDTO.getEmpId())
                .append(", leaveTypeMasterId : ").append(leaveDTO.getLeaveTypeMasterId())
                .append(", leaveTypeCode : ").append(leaveDTO.getLeaveTypeCode())
                .append(", noOfDays : ").append(leaveDTO.getNoOfDays())
                .append(", createdBy : ").append(leaveDTO.getCreatedBy());
        return logBuilder.toString();
    }
}
