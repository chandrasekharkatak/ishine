package com.apmosys.employeeportal.service.leave;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.Exception.LeaveApplicationException;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.service.CompOffLeaveService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Component
public class UpdatePendingLeaveValidator {

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private LeaveApplicationValidator leaveApplicationValidator;

    @Autowired
    private LeaveTypeMasterRepository leaveTypeMasterRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeavesMapRepository employeeLeavesMapRepository;

    @Autowired
    private CompOffLeaveService compOffLeaveService;

    @Autowired
    private StringToDateTimeParser stringToDateTimeParser;

    public UpdatePendingLeaveValidationResult validateForUpdate(LeaveDTO leaveDTO) {
        validateRequestPresent(leaveDTO);

        EmployeeLeave leaveToBeUpdated = employeeLeaveRepository.findById(leaveDTO.getLeaveId())
                .orElseThrow(() -> new LeaveApplicationException("Leave Application Not Found."));

        Optional<LeaveTypeMaster> leaveTypeOpt = leaveTypeMasterRepository.findById(leaveToBeUpdated.getLeaveTypeMasterId());
        if (!leaveTypeOpt.isPresent()) {
            throw new LeaveApplicationException("Invalid leave type.");
        }
        LeaveTypeMaster leaveType = leaveTypeOpt.get();

        EmployeeDTO employeeContext = loadEmployeeContext(leaveToBeUpdated.getEmpId());

        EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
                .findByEmpIdAndLeaveTypeMasterId(leaveToBeUpdated.getEmpId(), leaveToBeUpdated.getLeaveTypeMasterId());
        if (employeeLeavesMap == null) {
            throw new LeaveApplicationException("Leave balance not configured for this employee and leave type.");
        }

        double difference = computeDifference(leaveDTO, leaveToBeUpdated);
        validateCompOffAndBalance(leaveDTO, leaveType, employeeLeavesMap, difference);
        leaveApplicationValidator.validateCasualLeaveRulesForUpdate(leaveDTO);
        // leaveApplicationValidator.validatePrivilegeLeaveRulesForUpdate(leaveDTO);
        return new UpdatePendingLeaveValidationResult(
                leaveToBeUpdated,
                leaveType,
                employeeLeavesMap,
                employeeContext,
                difference,
                leaveToBeUpdated.getFromDate(),
                leaveToBeUpdated.getToDate());
    }

    private void validateRequestPresent(LeaveDTO leaveDTO) {
        if (leaveDTO == null || leaveDTO.getLeaveId() == null) {
            throw new LeaveApplicationException("leaveId is required.");
        }
        if (!StringUtils.hasText(leaveDTO.getFromDate()) || !StringUtils.hasText(leaveDTO.getToDate())) {
            throw new LeaveApplicationException("From date and To date are required.");
        }
        if (leaveDTO.getNoOfDays() == null) {
            throw new LeaveApplicationException("noOfDays is required.");
        }
    }

    private EmployeeDTO loadEmployeeContext(Long empId) {
        List<Object[]> empObj = employeeRepository.getManagerEmail(empId);
        EmployeeDTO empDto = new EmployeeDTO();
        if (empObj != null) {
            empObj.forEach((object) -> {
                empDto.setEmail(object[0] != null ? object[0].toString() : null);
                empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
                empDto.setName(object[2] != null ? object[2].toString() : null);
                empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
                empDto.setManagerName(object[4] != null ? object[4].toString() : null);
            });
        }
        return empDto;
    }

    // private int computeDifference(LeaveDTO leaveDTO, EmployeeLeave leaveToBeUpdated) {
    //     if (leaveDTO.getFromDate().equals(leaveToBeUpdated.getFromDate().toString())
    //             && leaveDTO.getToDate().equals(leaveToBeUpdated.getToDate().toString())) {
    //         return 0;
    //     }
    //     Period dbDateDifference = Period.between(leaveToBeUpdated.getFromDate(), leaveToBeUpdated.getToDate());
    //     LocalDate fromDate = stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd");
    //     LocalDate toDate = stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd");
    //     Period newDateDifference = Period.between(fromDate, toDate);
    //     return newDateDifference.getDays() - dbDateDifference.getDays();
    // }
    private double computeDifference(LeaveDTO leaveDTO, EmployeeLeave leaveToBeUpdated) {

        // Old values
        LocalDate oldFrom = leaveToBeUpdated.getFromDate();
        LocalDate oldTo = leaveToBeUpdated.getToDate();

        double oldFromDayType = leaveToBeUpdated.getFromDateDayType(); // 0 or 0.5
        double oldToDayType = leaveToBeUpdated.getToDateDayType();     // 0 or 0.5

        // New values
        LocalDate newFrom = stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd");
        LocalDate newTo = stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd");

        double newFromDayType = leaveDTO.getFromDateDayType(); // 0 or 0.5
        double newToDayType = leaveDTO.getToDateDayType();     // 0 or 0.5

        long oldDays = ChronoUnit.DAYS.between(oldFrom, oldTo) + 1;
        long newDays = ChronoUnit.DAYS.between(newFrom, newTo) + 1;

        double oldTotal;
        double newTotal;

        //  SAME DAY CASE
        if (oldFrom.equals(oldTo)) {
            oldTotal = 1 - Math.max(oldFromDayType, oldToDayType);
        } else {
            oldTotal = oldDays - oldFromDayType - oldToDayType;
        }

        if (newFrom.equals(newTo)) {
            newTotal = 1 - Math.max(newFromDayType, newToDayType);
        } else {
            newTotal = newDays - newFromDayType - newToDayType;
        }

        return  newTotal-oldTotal;
    }

    private void validateCompOffAndBalance(
            LeaveDTO leaveDTO,
            LeaveTypeMaster leaveType,
            EmployeeLeavesMap employeeLeavesMap,
            double difference) {
        float availableCompOffBalance = 0.0F;
        if ("CO".equalsIgnoreCase(leaveType.getLeaveTypeCode())) {
            ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
            if (compOffResponse != null && ServiceResponse.STATUS_SUCCESS.equals(compOffResponse.getServiceStatus())) {
                @SuppressWarnings("unchecked")
                List<LeaveDTO> availableCompOffList = (List<LeaveDTO>) compOffResponse.getServiceResponse();
                if (availableCompOffList != null) {
                    for (LeaveDTO compOff : availableCompOffList) {
                        availableCompOffBalance += (compOff.getNoOfDays() != null ? compOff.getNoOfDays() : 0f);
                    }
                }
            }
        }

        if (!"LWP".equalsIgnoreCase(leaveType.getLeaveTypeCode())
                 && difference > 0
        && (employeeLeavesMap.getBalance() == null
        || employeeLeavesMap.getBalance() < difference)) {
            throw new LeaveApplicationException("Your available balance of " + employeeLeavesMap.getBalance()
                    + " day(s) is not sufficient for this Leave Application.");
        }
        if ("CO".equalsIgnoreCase(leaveType.getLeaveTypeCode()) && availableCompOffBalance < difference) {
            throw new LeaveApplicationException("Your available Compensatory off balance of " + availableCompOffBalance
                    + " day(s) before " + leaveDTO.getFromDate() + " is not sufficient for this Leave Application.");
        }
    }
}
