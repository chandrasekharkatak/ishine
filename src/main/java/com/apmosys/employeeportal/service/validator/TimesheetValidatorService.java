package com.apmosys.employeeportal.service.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.service.TimesheetService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating timesheet data and business rules.
 * 
 * This service encapsulates all validation logic for timesheet operations:
 * - Authorization validation
 * - Date/time validation
 * - Document validation
 * - Business rule validation
 * 
 * @author Timesheet Refactoring - Phase 2
 */
@Slf4j
@Service
public class TimesheetValidatorService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
//    @Autowired
//    private TimesheetService timesheetService;

    /**
     * Validates that an employee is authorized to create/update a timesheet for another employee.
     * 
     * @param timesheetDTO The timesheet DTO containing empId and createdBy
     * @throws UnauthorizedAccessException if employee is not authorized
     */
    public void validateEmployeeAuthorization(TimesheetDTO timesheetDTO) {
        if (!Objects.equals(timesheetDTO.getEmpId(), timesheetDTO.getCreatedBy())) {
            log.debug("Validating authorization: empId={}, createdBy={}", 
                    timesheetDTO.getEmpId(), timesheetDTO.getCreatedBy());
            TimesheetService timesheetService = new TimesheetService();
            List<EmployeeDTO> teamList = timesheetService.getAllTeamMemberView(timesheetDTO.getCreatedBy());
            boolean isEmpPresent = teamList.stream()
                    .anyMatch(emp -> emp.getEmpId() != null && emp.getEmpId().equals(timesheetDTO.getEmpId()));
            
            if (!isEmpPresent) {
                log.warn("Unauthorized access attempt: empId={}, createdBy={}", 
                        timesheetDTO.getEmpId(), timesheetDTO.getCreatedBy());
                throw new UnauthorizedAccessException("Employee not authorized to perform this action");
            }
        }
    }

    /**
     * Validates that the timesheet date is not in the future.
     * 
     * @param timesheetDate The timesheet date to validate
     * @throws IllegalArgumentException if date is in the future
     */
    public void validateTimesheetDateNotInFuture(LocalDate timesheetDate) {
        if (timesheetDate.isAfter(LocalDate.now())) {
            log.warn("Timesheet date is in the future: {}", timesheetDate);
            throw new IllegalArgumentException("Timesheet date cannot be in the future.");
        }
    }

    /**
     * Validates timesheet lock period. If lock is enabled, timesheet cannot be created/updated
     * for dates before the lock period.
     * 
     * @param empId Employee ID
     * @param timesheetDate Timesheet date to validate
     * @return ServiceResponse with failure status if locked, null if validation passes
     */
    public ServiceResponse validateTimesheetLockPeriod(Long empId, LocalDate timesheetDate) {
        String isLockEnabled = employeeRepository.getIsLockEnabled(empId);
        
        if ("true".equalsIgnoreCase(isLockEnabled) && timesheetDate.isBefore(LocalDate.now().minusDays(1))) {
            log.warn("Timesheet is locked for empId={}, date={}", empId, timesheetDate);
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Your timesheet is locked");
            return response;
        }
        
        return null;
    }

    /**
     * Validates that client-side documents are provided when client-side ID is mandatory.
     * 
     * @param timesheetDTO The timesheet DTO
     * @param doc1 First document (optional)
     * @param doc2 Second document (optional)
     * @throws IllegalArgumentException if documents are missing when required
     */
    public void validateClientSideDocuments(TimesheetDTO timesheetDTO, 
                                            org.springframework.web.multipart.MultipartFile doc1,
                                            org.springframework.web.multipart.MultipartFile doc2) {
        // Skip validation for non-working day types
        if ("Public Holiday".equalsIgnoreCase(timesheetDTO.getDayType()) ||
            "Week Off".equalsIgnoreCase(timesheetDTO.getDayType()) ||
            "Leave".equalsIgnoreCase(timesheetDTO.getDayType()) ||
            "Client Holiday".equalsIgnoreCase(timesheetDTO.getDayType())) {
            return;
        }

        // Skip validation for shadow timesheets
        if ("Self".equalsIgnoreCase(timesheetDTO.getShadowFor())) {
            return;
        }

        Boolean isClientSideMandatory = projectRepository.getClientSideIdMandatory(timesheetDTO.getProjectId());
        
        if (Boolean.TRUE.equals(isClientSideMandatory) &&
            (doc1 == null || doc1.isEmpty()) &&
            (doc2 == null || doc2.isEmpty())) {
            log.warn("Client-side documents missing for mandatory project: projectId={}", 
                    timesheetDTO.getProjectId());
            throw new IllegalArgumentException("Client-side ID is mandatory, please upload required documents.");
        }
    }

    /**
     * Validates office in/out time constraints.
     * 
     * @param officeInTime Office in time
     * @param officeOutTime Office out time
     * @param currentTime Current time for comparison
     * @throws IllegalArgumentException if validation fails
     */
    public void validateOfficeTime(LocalDateTime officeInTime, LocalDateTime officeOutTime, LocalDateTime currentTime) {
        if (officeInTime.isAfter(currentTime)) {
            log.warn("Office in time is in the future: {}", officeInTime);
            throw new IllegalArgumentException("Office In Time cannot be greater than current time.");
        }
        
        if (officeOutTime.isAfter(currentTime)) {
            log.warn("Office out time is in the future: {}", officeOutTime);
            throw new IllegalArgumentException("Office Out Time cannot be greater than current time.");
        }
        
        if (officeInTime.isAfter(officeOutTime)) {
            log.warn("Office in time is after out time: in={}, out={}", officeInTime, officeOutTime);
            throw new IllegalArgumentException("Office Out Time cannot be less than than Office In Time.");
        }
    }

    /**
     * Validates client in/out time constraints.
     * 
     * @param clientInTime Client in time (nullable)
     * @param clientOutTime Client out time (nullable)
     * @param currentTime Current time for comparison
     * @return ServiceResponse with failure status if validation fails, null if validation passes
     */
    public ServiceResponse validateClientTime(LocalDateTime clientInTime, LocalDateTime clientOutTime, LocalDateTime currentTime) {
        if (clientInTime == null || clientOutTime == null) {
            log.warn("Client in/out time is null: in={}, out={}", clientInTime, clientOutTime);
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Client in time or out time is not provided");
            return response;
        }

        if (clientInTime.isAfter(clientOutTime)) {
            log.warn("Client in time is after out time: in={}, out={}", clientInTime, clientOutTime);
            throw new IllegalArgumentException("Client Out Time cannot be less than Client In Time.");
        }

        if (clientInTime.isAfter(currentTime) || clientOutTime.isAfter(currentTime)) {
            log.warn("Client in/out time is in the future: in={}, out={}, current={}", 
                    clientInTime, clientOutTime, currentTime);
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Client in time or out time cannot be greater than current date/time");
            return response;
        }

        return null;
    }

    /**
     * Validates client approval status is not null.
     * 
     * @param clientApprovalStatus Client approval status
     * @return ServiceResponse with failure status if null, null if validation passes
     */
    public ServiceResponse validateClientApprovalStatus(String clientApprovalStatus) {
        if (clientApprovalStatus == null) {
            log.warn("Client approval status is null");
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Client approval status is null");
            return response;
        }
        return null;
    }

    /**
     * Validates documents are provided based on client approval status.
     * 
     * @param timesheetDTO The timesheet DTO
     * @param doc1 First document (optional)
     * @param doc2 Second document (optional)
     * @return ServiceResponse with failure status if validation fails, null if validation passes
     */
    public ServiceResponse validateDocumentsForApprovalStatus(TimesheetDTO timesheetDTO,
                                                              org.springframework.web.multipart.MultipartFile doc1,
                                                              org.springframework.web.multipart.MultipartFile doc2) {
        String approvalStatus = timesheetDTO.getClientApprovalStatus();
        
        if (approvalStatus == null) {
            return validateClientApprovalStatus(null);
        }

        // For pending or approved status, doc1 is required
        if ((doc1 == null || doc1.isEmpty()) && 
            ("pending".equalsIgnoreCase(approvalStatus) || "approved".equalsIgnoreCase(approvalStatus))) {
            log.warn("Document missing for approval status: status={}", approvalStatus);
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("In case of pending/approved the filled timesheet document is missing");
            return response;
        }

        // For approved status, doc2 is also required
        if ((doc2 == null || doc2.isEmpty()) && "approved".equalsIgnoreCase(approvalStatus)) {
            log.warn("Approval document missing for approved status");
            ServiceResponse response = new ServiceResponse();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("In case of approved the approval document proof is missing");
            return response;
        }

        return null;
    }

    /**
     * Validates that activities list is not empty for working days.
     * 
     * @param activities List of activities
     * @return true if activities list is empty (validation warning), false otherwise
     */
    public boolean isActivitiesListEmpty(List<ActivityDTO> activities) {
        return activities == null || activities.isEmpty();
    }

    /**
     * Validates client-side time constraints for timesheet with client-side ID.
     * This is a comprehensive validation that checks all client-side time rules.
     * 
     * @param timesheetDTO The timesheet DTO
     * @param currentTime Current time for comparison
     * @return ServiceResponse with failure status if validation fails, null if validation passes
     */
    public ServiceResponse validateClientSideTimeConstraints(TimesheetDTO timesheetDTO, LocalDateTime currentTime) {
        // Only validate for non-shadow timesheets with client-side ID
        if (Boolean.FALSE.equals(timesheetDTO.getIsShadowTimesheet()) &&
            Boolean.TRUE.equals(timesheetDTO.getHasClientSideId()) &&
            ("Working".equalsIgnoreCase(timesheetDTO.getDayType()) ||
             "Non-working".equalsIgnoreCase(timesheetDTO.getDayType()))) {

            // Validate client times
            ServiceResponse timeValidation = validateClientTime(
                    timesheetDTO.getClientInTime(), 
                    timesheetDTO.getClientOutTime(), 
                    currentTime);
            if (timeValidation != null) {
                return timeValidation;
            }

            // Validate approval status
            ServiceResponse statusValidation = validateClientApprovalStatus(timesheetDTO.getClientApprovalStatus());
            if (statusValidation != null) {
                return statusValidation;
            }
        }

        return null;
    }
}

