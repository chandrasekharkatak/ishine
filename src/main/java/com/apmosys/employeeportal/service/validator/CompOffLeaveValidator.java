package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.Exception.CompOffLeaveException;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;

/**
 * Single entry point for comp-off request validation (CRUD-related APIs). Throws
 * {@link CompOffLeaveException} on failure.
 */
@Component
public class CompOffLeaveValidator {

	@Value("${compOff_apply_withIn}")
	private Long compOffApplyWithIn;

	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private HolidayRepository holidayRepository;
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new CompOffLeaveException(message);
		}
	}

	public void validateApplyForCompOff(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getEmpId() != null, "Employee id is required.");
		require(leaveDTO.getReasonId() != null, "Comp-off reason is required.");
		require(StringUtils.hasText(leaveDTO.getFromDate()), "From date is required.");
		LocalDate appliedForDate = parseIsoDateOrThrow(leaveDTO.getFromDate());
		LocalDate lastSeventhDate = LocalDate.now().minusDays(compOffApplyWithIn);
		LocalDate currentDate = LocalDate.now();
		require(!appliedForDate.isBefore(lastSeventhDate) && !appliedForDate.isAfter(currentDate),
				"Comp Off Date range exceeded !!");
				boolean pendingExists = compOffLeaveRepository
            .existsByEmpIdAndFromDateAndCompOffStatusIn(
                    leaveDTO.getEmpId(),
                    appliedForDate,
                   List.of("Pending", "Pending For Approval", "Approved")
            );
			Employee employee =
        employeeRepository
            .findByEmpId(
                leaveDTO.getEmpId());

    require(
        employee != null,
        "Employee not found."
    );

    require(
        employee.getDateOfJoining() != null,
        "Employee joining date not available."
    );

    require(
        !appliedForDate.isBefore(
            employee.getDateOfJoining()),
        "Comp-off cannot be applied for dates before joining date."
    );
			boolean isHoliday =holidayRepository.existsByDateOfHoliday(appliedForDate);

    require(
        isHoliday,
        "Selected date is not a valid holiday."
    );

    require(!pendingExists,
            "A pending comp-off request already exists for this date.");
	}

	public void validateGetPendingCompOffRequestsByManagerId(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getManagerId() != null, "Manager id is required.");
	}

	public void validateGetPendingCompOffRequestsByEmpId(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getEmpId() != null, "Employee id is required.");
	}

	public void validateGetAllCompOffRequestsByEmpId(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getEmpId() != null, "Employee id is required.");
	}

	public void validateCountPendingCompOffRequestsByManagerId(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getManagerId() != null, "Manager id is required.");
	}

	public void validateUpdateCompOffById(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getCompOffLeaveId() != null, "Comp-off leave id is required.");
		require(leaveDTO.getEmpId() != null, "Employee id is required.");
		require(leaveDTO.getLeaveStatusId() != null, "Leave status is required.");
		if (leaveDTO.getLeaveStatusId() == 3) {
			require(StringUtils.hasText(leaveDTO.getRejectCompOffReason()), "Reject reason is required.");
		}
		if (employeeRepository.findHodByEmpId(leaveDTO.getEmpId()).isEmpty()) {
			throw new CompOffLeaveException("No approver (HOD) found for employee.");
		}
	}

	public void validateUpdateCompOff(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getCompOffLeaveId() != null, "Comp-off leave id is required.");
		 require(leaveDTO.getEmpId() != null, "Employee id is required.");
		require(leaveDTO.getReasonId() != null, "Comp-off reason is required.");
		require(StringUtils.hasText(leaveDTO.getFromDate()), "From date is required.");
		parseIsoDateOrThrow(leaveDTO.getFromDate());
		require(leaveDTO.getReportingManagerId() != null, "Reporting manager id is required.");
		require(leaveDTO.getUpdatedBy() != null, "Updated by is required.");
		LocalDate appliedForDate = parseIsoDateOrThrow(leaveDTO.getFromDate());
		LocalDate lastSeventhDate = LocalDate.now().minusDays(compOffApplyWithIn);
		LocalDate currentDate = LocalDate.now();
		// require(!appliedForDate.isBefore(lastSeventhDate) && !appliedForDate.isAfter(currentDate),
				// "Comp Off Date range exceeded !!");
			CompOffLeave existing = compOffLeaveRepository
    .findByEmpIdAndFromDateAndCompOffStatusIn(
        leaveDTO.getEmpId(),
        appliedForDate,
        List.of("Pending", "Pending For Approval", "Approved")
    );

		if (existing != null &&
			!existing.getCompOffLeaveId().equals(leaveDTO.getCompOffLeaveId())) {
			throw new RuntimeException("A comp-off request already exists for this date.");
		}
		Employee employee =
        employeeRepository
            .findByEmpId(
                leaveDTO.getEmpId());

    require(
        employee != null,
        "Employee not found."
    );

    require(
        employee.getDateOfJoining() != null,
        "Employee joining date not available."
    );

    require(
        !appliedForDate.isBefore(
            employee.getDateOfJoining()),
        "Comp-off cannot be applied for dates before joining date."
    );
	boolean isHoliday =holidayRepository.existsByDateOfHoliday(appliedForDate);

    require(
        isHoliday,
        "Selected date is not a valid holiday."
    );

	}

	public void validateDeleteCompOff(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getCompOffLeaveId() != null, "Comp-off leave id is required.");
	}

	public void validateGetCompOffBalanceDetailsByEmpIdAndFromDate(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getEmpId() != null, "Employee id is required.");
		require(StringUtils.hasText(leaveDTO.getFromDate()), "From date is required.");
		parseIsoDateOrThrow(leaveDTO.getFromDate());
	}

	private static LocalDate parseIsoDateOrThrow(String fromDate) {
		try {
			return LocalDate.parse(fromDate.trim());
		} catch (DateTimeParseException e) {
			throw new CompOffLeaveException("Invalid from date (expected yyyy-MM-dd).");
		}
	}

	public void validateBulkCompOffRejectRequest(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getBulkLeaveRejectList() != null && !leaveDTO.getBulkLeaveRejectList().isEmpty(),
				"Bulk reject list is required.");
	}

	public void validateBulkCompOffApproveRequest(LeaveDTO leaveDTO) {
		require(leaveDTO != null, "Request body is required.");
		require(leaveDTO.getBulkLeaveApprovedList() != null && !leaveDTO.getBulkLeaveApprovedList().isEmpty(),
				"Bulk approve list is required.");
	}
}
