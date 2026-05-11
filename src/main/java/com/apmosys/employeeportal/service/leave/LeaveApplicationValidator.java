package com.apmosys.employeeportal.service.leave;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.Exception.LeaveApplicationException;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.enums.DayTypeCode;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.service.CompOffLeaveService;
import com.apmosys.employeeportal.service.LogService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Component
public class LeaveApplicationValidator {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationValidator.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private EmployeeLeavesMapRepository employeeLeavesMapRepository;

    @Autowired
    private LeaveTypeMasterRepository leaveTypeMasterRepository;

    @Autowired
    private CompOffLeaveService compOffLeaveService;

    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;

    @Autowired
    private TeamMemberQueryService teamMemberQueryService;

    @Autowired
    private LogService logService;

    @Autowired
    private HttpServletRequest httpRequest;

    @Value("${CLleave.maxDays}")
    private Long clLeaveDays;

    @Value("${leave.pl.max.days.per.month}")
    private double maxPlDaysPerMonth;

    public void validateRequestPresent(LeaveDTO dto) {
        if (dto == null) {
            throw new LeaveApplicationException("Invalid request payload");
        }
        if (dto.getEmpId() == null || dto.getCreatedBy() == null) {
            throw new LeaveApplicationException("empId and createdBy are required.");
        }
        if (!StringUtils.hasText(dto.getFromDate()) || !StringUtils.hasText(dto.getToDate())) {
            throw new LeaveApplicationException("From date and To date are required.");
        }
        if (dto.getLeaveTypeMasterId() == null) {
            throw new LeaveApplicationException("Invalid leave type.");
        }
        if (dto.getNoOfDays() == null) {
            throw new LeaveApplicationException("noOfDays is required.");
        }
    }

    public ParsedLeaveDates parseAndValidateDates(LeaveDTO dto) {
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(dto.getFromDate(), DATE_FMT);
            toDate = LocalDate.parse(dto.getToDate(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new LeaveApplicationException("Invalid date format. Please use yyyy-MM-dd.");
        }
        if (fromDate.isAfter(toDate)) {
            throw new LeaveApplicationException("From date cannot be after To date.");
        }
        return new ParsedLeaveDates(fromDate, toDate);
    }
    public void validateLeaveTypeAllowedByPolicy(LeaveDTO dto) {

    List<Object[]> allowedLeaveTypes =
            leaveTypeMasterRepository.getAllLeaveTypesByLeavePolicies(
                    dto.getEmploymentStatus(),
                    dto.getGender(),
                    dto.getMaritalStatus()
            );

    boolean allowed = allowedLeaveTypes.stream()
            .anyMatch(row ->
                    row[0] != null &&
                    row[0].toString().equals(dto.getLeaveTypeMasterId().toString())
            );

    if (!allowed) {
        throw new LeaveApplicationException(
                "Selected leave type is not available"
        );
    }
}
    public void validateAuthorizedToApply(LeaveDTO dto, String logPayload) {
        boolean isSelfApply = Objects.equals(dto.getCreatedBy(), dto.getEmpId());
        if (isSelfApply) {
            return;
        }
        List<Long> teamList = teamMemberQueryService.getTeamMemberEmpIds(dto.getCreatedBy());
        boolean isTeamMate = teamList.stream()
                .anyMatch(emp -> emp != null && emp.equals(dto.getEmpId()));
        if (!isTeamMate) {
            log.warn("Unauthorized leave apply attempt for empId {} by {}", dto.getEmpId(), dto.getCreatedBy());
            LogDTO apiLogInfo = new LogDTO();
            apiLogInfo.setSubFeatureName("Apply Leave");
            apiLogInfo.setApiUrl("/api/applyLeave");
            apiLogInfo.setLogLevel("INFO");
            apiLogInfo.setApiResponse("Unauthorized leave apply attempt for empId: " + dto.getEmpId());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiRequest(logPayload);
            logService.logMyInfo(httpRequest, apiLogInfo);
            throw new LeaveApplicationException("You are not authorized to apply leave for this employee.");
        }
    }

    public void validateNoOverlappingLeave(Long empId, LocalDate fromDate, LocalDate toDate, String logPayload) {
        boolean overlapping = employeeLeaveRepository.existsOverlappingLeave(empId, fromDate, toDate);
        if (overlapping) {
            LogDTO apiLogInfo = new LogDTO();
            apiLogInfo.setSubFeatureName("Apply Leave");
            apiLogInfo.setApiUrl("/api/applyLeave");
            apiLogInfo.setLogLevel("INFO");
            apiLogInfo.setApiResponse("Leave details are already present for selected dates.");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiRequest(logPayload);
            logService.logMyInfo(httpRequest, apiLogInfo);
            throw new LeaveApplicationException("You already have an existing leave during the selected dates.");
        }
    }

    public LeaveMapAndType loadLeaveMapAndType(LeaveDTO dto) {
        EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
                .findByEmpIdAndLeaveTypeMasterId(dto.getEmpId(), dto.getLeaveTypeMasterId());
        if (employeeLeavesMap == null) {
            throw new LeaveApplicationException("Leave balance not configured for this employee and leave type.");
        }
        Optional<LeaveTypeMaster> leavetypeOpt = leaveTypeMasterRepository.findById(dto.getLeaveTypeMasterId());
        if (!leavetypeOpt.isPresent()) {
            throw new LeaveApplicationException("Invalid leave type.");
        }
        return new LeaveMapAndType(employeeLeavesMap, leavetypeOpt.get());
    }

    public void validateCompOffBalanceIfApplicable(LeaveDTO dto) {
        if (!"CO".equalsIgnoreCase(dto.getLeaveTypeCode())) {
            return;
        }
        float availableCompOffBalance = 0.0F;
        ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(dto);
        if (compOffResponse != null && ServiceResponse.STATUS_SUCCESS.equals(compOffResponse.getServiceStatus())) {
            @SuppressWarnings("unchecked")
            List<LeaveDTO> availableCompOffList = (List<LeaveDTO>) compOffResponse.getServiceResponse();
            if (availableCompOffList != null) {
                for (LeaveDTO compOff : availableCompOffList) {
                    availableCompOffBalance += (compOff.getNoOfDays() != null ? compOff.getNoOfDays() : 0f);
                }
            }
        }
        if (availableCompOffBalance < dto.getNoOfDays()) {
            throw new LeaveApplicationException(
                    "Your available Compensatory off balance of " + availableCompOffBalance
                            + " day(s) before " + dto.getFromDate() + " is not sufficient for this Leave Application.");
        }
    }

    public void validateSufficientBalanceUnlessLwp(LeaveDTO dto, EmployeeLeavesMap employeeLeavesMap) {
        if ("LWP".equalsIgnoreCase(dto.getLeaveTypeCode())) {
            return;
        }
        if (employeeLeavesMap.getBalance() == null || employeeLeavesMap.getBalance() == 0
                || employeeLeavesMap.getBalance() < dto.getNoOfDays()) {
            throw new LeaveApplicationException(
                    "Your available balance of " + employeeLeavesMap.getBalance()
                            + " day(s) is not sufficient for this Leave Application.");
        }
    }

    public void validateCasualLeaveRules(LeaveDTO dto) {
        if (!"CL".equalsIgnoreCase(dto.getLeaveTypeCode())) {
            return;
        }
        if (dto.getNoOfDays() > clLeaveDays) {
            throw new LeaveApplicationException("Casual leave cannot be taken for more than " + clLeaveDays + " days");
        }
        YearMonth appliedMonth = YearMonth.from(LocalDate.parse(dto.getFromDate()));
        List<EmployeeLeave> clLeavesThisMonth = employeeLeaveRepository.findByEmpIdAndLeaveTypeAndMonth(
                dto.getEmpId(),
                dto.getLeaveTypeMasterId(),
                appliedMonth.getYear(),
                appliedMonth.getMonthValue());
        double totalCLDaysThisMonth = clLeavesThisMonth.stream()
                .filter(leave ->
                        leave.getLeaveStatusId() != null
                                && (leave.getLeaveStatusId() == 1 || leave.getLeaveStatusId() == 2))
                .mapToDouble(EmployeeLeave::getNoOfDays)
                .sum();
        if (totalCLDaysThisMonth + dto.getNoOfDays() > 2.0) {
            throw new LeaveApplicationException(
                    "Casual Leave cannot exceed 2 days in a month. Already applied: "
                            + totalCLDaysThisMonth + " days.");
        }
    }

public void validatePrivilegeLeaveRules(LeaveDTO dto) {

    if (!"PL".equalsIgnoreCase(dto.getLeaveTypeCode())) {
        return;
    }

    if (dto.getNoOfDays() > maxPlDaysPerMonth) {

        throw new LeaveApplicationException(
                "Privilege Leave cannot be taken for more than "
                        + maxPlDaysPerMonth + " days at once");
    }

    YearMonth appliedMonth = YearMonth.from(LocalDate.parse(dto.getFromDate()));

    List<EmployeeLeave> plLeavesThisMonth =
            employeeLeaveRepository.findByEmpIdAndLeaveTypeAndMonth(
                    dto.getEmpId(),
                    dto.getLeaveTypeMasterId(),
                    appliedMonth.getYear(),
                    appliedMonth.getMonthValue());

    double totalPLDaysThisMonth = plLeavesThisMonth.stream()
            .filter(leave ->
                    leave.getLeaveStatusId() != null
                            && (leave.getLeaveStatusId() == 1
                            || leave.getLeaveStatusId() == 2))
            .mapToDouble(EmployeeLeave::getNoOfDays)
            .sum();

    if (totalPLDaysThisMonth + dto.getNoOfDays()
            > maxPlDaysPerMonth) {

        throw new LeaveApplicationException(
                "Privilege Leave cannot exceed "
                        + maxPlDaysPerMonth
                        + " days in a month. Already applied: "
                        + totalPLDaysThisMonth + " days.");
    }
}
    public void validateCasualLeaveRulesForUpdate(LeaveDTO dto) {
        if (!"Casual Leave".equalsIgnoreCase(dto.getLeaveType())) {
            return;
        } 
        if (dto.getNoOfDays() > clLeaveDays) {
            throw new LeaveApplicationException("Casual leave cannot be taken for more than " + clLeaveDays + " days");
        }
        YearMonth appliedMonth = YearMonth.from(LocalDate.parse(dto.getFromDate()));
        List<EmployeeLeave> clLeavesThisMonth = employeeLeaveRepository.findByEmpIdAndLeaveTypeAndMonth(
                dto.getEmpId(),
                dto.getLeaveTypeMasterId(),
                appliedMonth.getYear(),
                appliedMonth.getMonthValue());
        double totalCLDaysThisMonth = clLeavesThisMonth.stream()
        .filter(leave ->
                leave.getLeaveStatusId() != null
                        && (leave.getLeaveStatusId() == 1 || leave.getLeaveStatusId() == 2)
                        // 👇 Exclude current leave being updated
                        && (dto.getLeaveId() == null || !leave.getLeaveId().equals(dto.getLeaveId()))
        )
        .mapToDouble(EmployeeLeave::getNoOfDays)
        .sum();
        if (totalCLDaysThisMonth + dto.getNoOfDays() > 2.0) {
            throw new LeaveApplicationException(
                    "Casual Leave cannot exceed 2 days in a month. Already applied: "
                            + totalCLDaysThisMonth + " days.");
        }
    }

    /**
     * Ensures leave day type exists before persisting leave + timesheet side effects (fail fast, transactional rollback).
     */
    public DayTypeMasterNew requireLeaveDayTypeForTimesheet() {
        DayTypeMasterNew leaveDayType = dayTypeMasterNewRepository.findByDayType(DayTypeCode.LEAVE.getDbValue());
        if (leaveDayType == null) {
            throw new LeaveApplicationException(
                    "DayType '" + DayTypeCode.LEAVE.getDbValue() + "' not found in day_type_master_new.");
        }
        return leaveDayType;
    }
}
