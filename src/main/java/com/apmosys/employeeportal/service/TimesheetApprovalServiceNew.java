package com.apmosys.employeeportal.service;
	
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.dto.BulkTimesheetRequestDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeTimesheetsNewDTO;
import com.apmosys.employeeportal.dto.GetMyReporteesTimesheetRequestsPayload;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectRejectionDTO;
import com.apmosys.employeeportal.dto.SkippedTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetApprovalNewDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.dto.TimesheetRejectionReasonsMasterDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqFlatDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.ProjectTimesheetStatusNew;
import com.apmosys.employeeportal.model.TimesheetActionAuditNew;
import com.apmosys.employeeportal.model.TimesheetApprovalAllocationLogs;
import com.apmosys.employeeportal.model.TimesheetDocumentApproval;
import com.apmosys.employeeportal.model.TimesheetRejectionDetailsId;
import com.apmosys.employeeportal.model.TimesheetRejectionDetailsNew;
import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
import com.apmosys.employeeportal.repository.TimesheetActionAuditNewRepository;
import com.apmosys.employeeportal.repository.TimesheetApprovalAllocationLogsRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.repository.TimesheetRejectionDetailsNewRepository;
import com.apmosys.employeeportal.repository.TimesheetRejectionReasonsMasterRepository;
import com.apmosys.employeeportal.service.helper.TimesheetAggregationHelper;
import com.apmosys.employeeportal.service.mapper.TimesheetMapper;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing timesheet approval operations.
 * 
 * This service encapsulates all approval-related operations for timesheets:
 * - Approval/rejection of timesheets
 * - Bulk approval/rejection operations
 * - Approval workflow management
 * - Rejection reason management
 * - Manager reportee timesheet requests
 * 
 * @author Timesheet Refactoring - Phase 4
 */
@Slf4j
@Service
public class TimesheetApprovalServiceNew {
	
	public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_APPROVED = 2;
    public static final Integer STATUS_REJECTED = 3;
    public static final String ACTION_TYPE_APPROVAL = "APPROVAL";
    public static final String ACTION_TYPE_REJECTION = "REJECTION";
    
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimesheetDocumentApprovalRepository timesheetDocumentApprovalRepository;
    
    @Autowired
    private TimesheetApprovalAllocationLogsRepository timesheetApprovalAllocationLogsRepository;
    
    @Autowired
    private TimesheetRejectionReasonsMasterRepository timesheetRejectionReasonsMasterRepository;
    
    @Autowired
    private TimesheetDocumentDetailsRepository timesheetDocumentDetailsRepository;
    
    @Autowired
    private ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;
    
    @Autowired
    private TimesheetActionAuditNewRepository timesheetActionAuditNewRepository;
    
    @Autowired
    private TimesheetRejectionDetailsNewRepository timesheetRejectionDetailsNewRepository;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;
    
    @Autowired
     private TimesheetMapper timesheetMapper;
    
    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    /**
     * Gets timesheet rbulkApproveTimesheetsByIdsequests for manager's reportees.
     * 
     * @param timesheetDTO Contains managerId and status filter
     * @return ServiceResponse with list of timesheet requests
     * 
     * 
     */
    
    /**
     * Bulk approve timesheets by TIMESHEET IDs only.
     *
     * @param TimesheetApprovalNewDTO 
     * @return ServiceResponse
     * Steps followed:
     * 1. Check for valid Arguments
     * 2. Check for valid Approver
     * 3. Check for valid Timesheets
     * 4. Check for valid related project status data
     * 5. Update status of timehseets
     * 6. Update status of project status data
     * 7. Save Timesheets and Project status data
     * 8. Create action logs for each projec stauts
     * 9. Save all action logs
     * 
     */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse bulkApproveTimesheets(TimesheetApprovalNewDTO dto) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("bulk_approve_timesheets");
		apiLogInfo.setApiUrl("/api/bulkApproveTimesheets");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("rmId : ").append(dto.getRmId()).append(" | statusId : ").append(dto.getStatusId())
				.append(" | timesheetIds : ").append(dto.getTimesheetIds());
		try {
			if (dto.getStatusId() == null|| dto.getStatusId() != STATUS_APPROVED)
				throw new IllegalArgumentException("Proper Status is not provided for the request");

			List<EmployeeTimesheetsNew> timesheetDatas = employeeTimesheetsNewRepository
					.findAllById(dto.getTimesheetIds());
			List<EmployeeTimesheetsNew> rmIdMismatchList = timesheetDatas.stream()
					.filter(data -> data.getCurrentManagerId() != dto.getRmId()).collect(Collectors.toList());
			if (rmIdMismatchList.size() > 0)
				throw new BadRequestException("You are not the Approver of some timesheets");

			Set<Long> existingTimesheetIds = timesheetDatas.stream().map(EmployeeTimesheetsNew::getTimesheetId)
					.filter(Objects::nonNull).collect(Collectors.toSet());

			List<Long> filteredIds = dto.getTimesheetIds().stream().filter(id -> !existingTimesheetIds.contains(id))
					.collect(Collectors.toList());

			if (filteredIds.size() > 0)
				throw new BadRequestException("No timesheets found for the timesheet ids: " + filteredIds);

			List<ProjectTimesheetStatusNew> projectTimesheets = projectTimesheetStatusNewRepository
					.findAllByIdTimesheetIdIn(existingTimesheetIds);

			Set<Long> timesheetIdsWithProjects = projectTimesheets.stream().map(p -> p.getId().getTimesheetId())
					.collect(Collectors.toSet());

			List<Long> timesheetsWithNoProjects = existingTimesheetIds.stream()
					.filter(id -> !timesheetIdsWithProjects.contains(id)).collect(Collectors.toList());

			if (!timesheetsWithNoProjects.isEmpty() || timesheetsWithNoProjects.size() > 0)
				throw new BadRequestException(
						"No project entries found for timesheet ids: " + timesheetsWithNoProjects);

			projectTimesheets.forEach(p -> p.setStatus(STATUS_APPROVED));
			timesheetDatas.forEach(t -> t.setStatus(STATUS_APPROVED));

			List<ProjectTimesheetStatusNew> approvedProjects = projectTimesheetStatusNewRepository
					.saveAll(projectTimesheets);
			employeeTimesheetsNewRepository.saveAll(timesheetDatas);
			Set<TimesheetActionAuditNew> actionAudit = new HashSet<>();
			if (approvedProjects.size() > 0) {
				approvedProjects.forEach(p -> {
					TimesheetActionAuditNew audit = new TimesheetActionAuditNew();
					audit.setTimesheetId(p.getId().getTimesheetId());
					audit.setProjectId(p.getId().getProjectId());
					audit.setActionBy(dto.getRmId());
					audit.setActionOn(LocalDateTime.now());
					audit.setActionType(ACTION_TYPE_APPROVAL);
					actionAudit.add(audit);
				});
			}
			timesheetActionAuditNewRepository.saveAll(actionAudit);
			response.setServiceResponse("All the timesheets are approved successfully | Approved projects count : " + approvedProjects.size());
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Approved timesheets count : " + timesheetDatas.size()
					+ " | Approved projects count : " + approvedProjects.size());
		} catch (BadRequestException | IllegalArgumentException e) {

			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");

			throw e;

		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");

			throw new RuntimeException("Bulk approval failed", e);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		return response;
	}
    
	/**
     * Bulk Reject timesheets by TIMESHEET IDs only.
     *
     * @param TimesheetApprovalNewDTO 
     * @return ServiceResponse
     * Steps followed:
     * 1. Check for valid Arguments
     * 2. Check for valid Approver
     * 3. Check for valid Timesheets
     * 4. Check for valid related project status data
     * 5. Update status of timehseets
     * 6. Update status of project status data
     * 7. Save Timesheets and Project status data
     * 8. Crate and save rejection details
     * 9. Create action logs for each projec stauts
     * 10. Save all action logs
     * Missing validation:
     * 1. Rejection id validation from Rejection master table which is not there for new module
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse bulkRejectTimesheets(TimesheetApprovalNewDTO dto) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("bulk_approve_timesheets");
		apiLogInfo.setApiUrl("/api/bulkApproveTimesheets");
		apiLogInfo.setLogLevel("INFO");

		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("rmId : ").append(dto.getRmId()).append(" | statusId : ").append(dto.getStatusId())
				.append(" | timesheetIds : ").append(dto.getTimesheetIds());
		try {
			if (dto.getStatusId() == null || dto.getStatusId() != STATUS_REJECTED)throw new IllegalArgumentException("Proper status is not provided for the request");
			
			if (dto.getRejectionReasonId() == null) throw new IllegalArgumentException("Rejection reason not provided");
			
			List<EmployeeTimesheetsNew> timesheetDatas = employeeTimesheetsNewRepository
					.findAllById(dto.getTimesheetIds());
			List<EmployeeTimesheetsNew> rmIdMismatchList = timesheetDatas.stream()
					.filter(data -> data.getCurrentManagerId() != dto.getRmId()).collect(Collectors.toList());
			if (rmIdMismatchList.size() > 0)
				throw new BadRequestException("You do not have the rejection rights of some timesheets");

			Set<Long> existingTimesheetIds = timesheetDatas.stream().map(EmployeeTimesheetsNew::getTimesheetId)
					.filter(Objects::nonNull).collect(Collectors.toSet());

			List<Long> filteredIds = dto.getTimesheetIds().stream().filter(id -> !existingTimesheetIds.contains(id))
					.collect(Collectors.toList());

			if (filteredIds.size() > 0)
				throw new BadRequestException("No timesheets found for the timesheet ids: " + filteredIds);

			List<ProjectTimesheetStatusNew> projectTimesheets = projectTimesheetStatusNewRepository
					.findAllByIdTimesheetIdIn(existingTimesheetIds);

			Set<Long> timesheetIdsWithProjects = projectTimesheets.stream().map(p -> p.getId().getTimesheetId())
					.collect(Collectors.toSet());

			List<Long> timesheetsWithNoProjects = existingTimesheetIds.stream()
					.filter(id -> !timesheetIdsWithProjects.contains(id)).collect(Collectors.toList());

			if (!timesheetsWithNoProjects.isEmpty() || timesheetsWithNoProjects.size() > 0)
				throw new BadRequestException(
						"No project entries found for timesheet ids: " + timesheetsWithNoProjects);

			projectTimesheets.forEach(p -> p.setStatus(STATUS_REJECTED));
			timesheetDatas.forEach(t -> t.setStatus(STATUS_REJECTED));

			List<ProjectTimesheetStatusNew> rejectedProjects = projectTimesheetStatusNewRepository
					.saveAll(projectTimesheets);
			employeeTimesheetsNewRepository.saveAll(timesheetDatas);
			
			Set<TimesheetRejectionDetailsNew> rejectionMappings = new HashSet<>();
			
			Set<TimesheetActionAuditNew> actionAudit = new HashSet<>();
			if (rejectedProjects.size() > 0) {
				rejectedProjects.forEach(p -> {
					TimesheetActionAuditNew audit = new TimesheetActionAuditNew();
					TimesheetRejectionDetailsNew rejectionMap = new TimesheetRejectionDetailsNew();
					TimesheetRejectionDetailsId rejectionMapId = new TimesheetRejectionDetailsId();
					
					// Audit action data making
					audit.setTimesheetId(p.getId().getTimesheetId());
					audit.setProjectId(p.getId().getProjectId());
					audit.setActionBy(dto.getRmId());
					audit.setActionOn(LocalDateTime.now());
					audit.setActionType(ACTION_TYPE_REJECTION);
					actionAudit.add(audit);
					

			        rejectionMap.setTimesheetId(p.getId().getTimesheetId());
			        rejectionMap.setProjectId(p.getId().getProjectId());
			        rejectionMap.setLocationMappingId(p.getId().getLocationMappingId());
			        rejectionMap.setRejectionId(dto.getRejectionReasonId());
			        rejectionMap.setRemarks(dto.getRemarks());
			        rejectionMap.setRejectedBy(dto.getRmId());
			        rejectionMap.setRejectedOn(LocalDateTime.now());

			        rejectionMappings.add(rejectionMap);
				});
			}
			
			timesheetRejectionDetailsNewRepository.saveAll(rejectionMappings);
			timesheetActionAuditNewRepository.saveAll(actionAudit);
			response.setServiceResponse("All the timesheets are rejected successfully | Rejected timesheets count : " + timesheetDatas.size());
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Rejected timesheets count : " + timesheetDatas.size()
					+ " | Rejected projects count : " + rejectedProjects.size()
					+ " | Rejection id: "+ dto.getRejectionReasonId());
		} catch (BadRequestException | IllegalArgumentException e) {

			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");

			throw e;

		} catch (Exception e) {

			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			response.setServiceError(e.getMessage());

			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setLogLevel("ERROR");

			throw new RuntimeException("Bulk rejection failed", e);
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		return response;
	}

    public ServiceResponse bulkOrSingleApproveOrReject(BulkTimesheetRequestDTO request) {

	    ServiceResponse response = new ServiceResponse();

	    try {
	        String status = request.getStatus();
	        List<Long> timesheetIdsReq = request.getTimesheetIds();

	        List<EmployeeTimesheetsNew> timesheetDatas =
	                employeeTimesheetsNewRepository.findAllById(timesheetIdsReq);

	        boolean hasMismatch = timesheetDatas.stream()
	                .anyMatch(data -> !java.util.Objects.equals(data.getCurrentManagerId(), request.getRmId()));

	        if (hasMismatch) {
	            throw new BadRequestException("You do not have the approval/rejection rights of some timesheets");
	        }

//	        if ("REJECTED".equalsIgnoreCase(status) && timesheetIdsReq.size() > 1) {
//	            throw new IllegalArgumentException("Only one timesheet can be rejected at a time.");
//	        }

	        List<EmployeeTimesheetsNewDTO> timesheets =
	                employeeTimesheetsNewRepository.fetchTimesheetsWithEmploymentId(timesheetIdsReq);

	        List<SkippedTimesheetDTO> skippedTimesheets = new ArrayList<>();
	        List<Long> validTimesheetIds = new ArrayList<>();

	        for (EmployeeTimesheetsNewDTO ts : timesheets) {

	            String prefix = "true".equalsIgnoreCase(ts.getIsProd()) ? "AP-" : "A-";
	            String formattedEmpId = prefix + ts.getEmployementID();

	            Integer tsStatus = ts.getStatus();
	            if (tsStatus != null && tsStatus == 1) {
	                validTimesheetIds.add(ts.getTimesheetId());
	            } else if (tsStatus != null && tsStatus == 2) {
	                skippedTimesheets.add(new SkippedTimesheetDTO(
	                        ts.getTimesheetId(),
	                        formattedEmpId,
	                        ts.getDate(),
	                        "Already Approved",
							ts.getEmpId()
	                ));
	            } else if (tsStatus != null && tsStatus == 3) {
	                skippedTimesheets.add(new SkippedTimesheetDTO(
	                        ts.getTimesheetId(),
	                        formattedEmpId,
	                        ts.getDate(),
	                        "Already Rejected",
							ts.getEmpId()
	                ));
	            }
	        }

	        if (validTimesheetIds.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No valid timesheets to process");
	            response.setServiceError(skippedTimesheets);
	            return response;
	        }

	        List<Object[]> result =
	                projectTimesheetStatusNewRepository.findProjectsForTimesheetIds(validTimesheetIds);

	        Map<Long, List<Long>> timesheetProjectMap = new HashMap<>();
	        for (Object[] r : result) {
	            Long timesheetId = ((Number) r[0]).longValue();
	            Long projectId = ((Number) r[1]).longValue();
	            timesheetProjectMap.computeIfAbsent(timesheetId, k -> new ArrayList<>()).add(projectId);
	        }

	        Long updatedBy = request.getUpdatedBy();

	        if ("APPROVED".equalsIgnoreCase(status)) {
	            saveAuditForApproval(validTimesheetIds, timesheetProjectMap, updatedBy, status);
	        } else if ("REJECTED".equalsIgnoreCase(status)) {
	            saveRejectionDetails(request);
	        }

	        Map<String, Object> finalResponse = new HashMap<>();
	        finalResponse.put("processed", validTimesheetIds);
	        finalResponse.put("skipped", skippedTimesheets);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(finalResponse);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Something went wrong while processing timesheets");
	    }

	    return response;
	}

	private void saveAuditForApproval(List<Long> timesheetIds, Map<Long, List<Long>> timesheetProjectMap,
	                                  Long updatedBy, String status) {

	    List<TimesheetActionAuditNew> auditList = new ArrayList<>();
	    LocalDateTime now = LocalDateTime.now();
	    int statusValue = 2;

	    for (Map.Entry<Long, List<Long>> entry : timesheetProjectMap.entrySet()) {

	        Long timesheetId = entry.getKey();
	        List<Long> projectIds = entry.getValue();

	        for (Long projectId : projectIds) {
	            TimesheetActionAuditNew audit = new TimesheetActionAuditNew();
	            audit.setTimesheetId(timesheetId);
	            audit.setProjectId(projectId.intValue());
	            audit.setActionType(status);
	            audit.setActionBy(updatedBy);
	            audit.setActionOn(now);
	            auditList.add(audit);
	        }
	    }

	    timesheetActionAuditNewRepository.saveAll(auditList);
	    employeeTimesheetsNewRepository.processByStatus(timesheetIds, statusValue);
	    projectTimesheetStatusNewRepository.processByStatus(timesheetIds, statusValue);
	}

	private void saveRejectionDetails(BulkTimesheetRequestDTO request) {

	    List<TimesheetRejectionDetailsNew> rejectionList = new ArrayList<>();
	    List<TimesheetActionAuditNew> auditList = new ArrayList<>();
	    LocalDateTime now = LocalDateTime.now();

	    List<Long> timesheetIds = request.getTimesheetIds();
	    Long updatedBy = request.getUpdatedBy();

	    List<ProjectRejectionDTO> projectRejections = request.getProjectRejections();

	    for (Long timesheetId : timesheetIds) {

	        for (ProjectRejectionDTO pr : projectRejections) {

	            List<Long> projectIds = pr.getProjectIds();
	            List<Long> rejectionIds = pr.getRejectionIds();
	            String remark = pr.getRejectRemark();

	            for (Long projectId : projectIds) {

	                List<Long> locationMappingIds = projectTimesheetStatusNewRepository
	                        .findLocationMappingId(timesheetId, projectId.intValue());

	                projectTimesheetStatusNewRepository
	                        .processByTSandProject(timesheetId, projectId.intValue(), 3);

	                TimesheetActionAuditNew audit = new TimesheetActionAuditNew();
	                audit.setTimesheetId(timesheetId);
	                audit.setProjectId(projectId.intValue());
	                audit.setActionType("REJECTED");
	                audit.setActionBy(updatedBy);
	                audit.setActionOn(now);
	                auditList.add(audit);

				for (Long locationMappingId : locationMappingIds) {
	                for (Long rejectionId : rejectionIds) {
	                    TimesheetRejectionDetailsNew rejection = new TimesheetRejectionDetailsNew();
	                    rejection.setTimesheetId(timesheetId);
	                    rejection.setLocationMappingId(locationMappingId);
	                    rejection.setProjectId(projectId.intValue());
	                    rejection.setRejectionId(rejectionId);
	                    rejection.setRemarks(remark);
	                    rejection.setRejectedBy(updatedBy);
	                    rejection.setRejectedOn(now);
	                    rejectionList.add(rejection);
	                }
	            }
			}
	        }
	    }

	    timesheetActionAuditNewRepository.saveAll(auditList);
	    employeeTimesheetsNewRepository.processByStatus(timesheetIds, 3);
	    timesheetRejectionDetailsNewRepository.saveAll(rejectionList);
	}
    
}

