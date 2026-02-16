	package com.apmosys.employeeportal.service;
	
	import java.sql.Timestamp;
	import java.time.LocalDateTime;
	import java.time.format.DateTimeFormatter;
	import java.util.ArrayList;
	import java.util.Collections;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.Objects;
	import java.util.Optional;
	import java.util.function.Function;
	import java.util.stream.Collectors;
	
	import javax.servlet.http.HttpServletRequest;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.data.domain.Page;
	import org.springframework.data.domain.PageRequest;
	import org.springframework.data.domain.Pageable;
	import org.springframework.data.domain.Sort;
	import org.springframework.data.jpa.domain.JpaSort;
	import org.springframework.stereotype.Service;
	import org.springframework.transaction.annotation.Transactional;
	
	import com.apmosys.employeeportal.dto.EmployeeDTO;
	import com.apmosys.employeeportal.dto.GetMyReporteesTimesheetRequestsPayload;
	import com.apmosys.employeeportal.dto.LogDTO;
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
	import com.apmosys.employeeportal.model.Timesheet;
	import com.apmosys.employeeportal.model.TimesheetApprovalAllocationLogs;
	import com.apmosys.employeeportal.model.TimesheetDocumentApproval;
	import com.apmosys.employeeportal.model.TimesheetRejectionReasonsMaster;
	import com.apmosys.employeeportal.repository.EmployeeRepository;
	import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
	import com.apmosys.employeeportal.repository.TimesheetApprovalAllocationLogsRepository;
	import com.apmosys.employeeportal.repository.TimesheetDocumentApprovalRepository;
	import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
	import com.apmosys.employeeportal.repository.TimesheetRejectionReasonsMasterRepository;
	import com.apmosys.employeeportal.repository.TimesheetsRepository;
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
	public class TimesheetApprovalService {
	
	    @Autowired
	    private TimesheetsRepository timesheetsRepository;
	    
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
	     * @param timesheetIds list of timesheetIds
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse bulkApproveTimesheetsByIds(List<Long> timesheetIds) {
	
	        ServiceResponse response = new ServiceResponse();
	
	        try {
	            if (timesheetIds == null || timesheetIds.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No timesheet IDs provided");
	                return response;
	            }
	
	            List<Timesheet> timesheets =
	                    timesheetsRepository.findAllById(timesheetIds);
	
	            if (timesheets.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No matching timesheets found");
	                return response;
	            }
	
	            for (Timesheet ts : timesheets) {
	                ts.setStatus("Approved");
	                ts.setTimesheetStatusUpdatedBy(
	                        Long.parseLong(httpRequest.getHeader("empId") == null
	                                ? "0"
	                                : httpRequest.getHeader("empId"))
	                );
	            }
	
	            timesheetsRepository.saveAll(timesheets);
	
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(
	                    "Approved " + timesheets.size() + " timesheets successfully"
	            );
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Bulk approval failed");
	            response.setServiceError(e.getMessage());
	        }
	
	        return response;
	    }
	
	    public ServiceResponse getMyReporteesTimesheetRequestsOld(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
	        apiLogInfo.setApiUrl("/api/getMyReporteesTimesheetRequests");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("managerId : " + timesheetDTO.getManagerId() + " ,status : " + timesheetDTO.getStatus());
	        
	        try {
	            List<Object[]> objectList = null;
	            Boolean clientFlag = timesheetDTO.getClient() != null && timesheetDTO.getClient() ? true : null;
	            
	            if (timesheetDTO.getManagerId() != null) {
	                Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId());
	                objectList = timesheetsRepository.getMyReporteesTimesheetRequestsOLD(
	                        timesheetDTO.getManagerId(), timesheetDTO.getStatus(), 
	                        employeeData.getDateOfJoining(), clientFlag);
	            } else {
	                objectList = timesheetsRepository.getMyTimesheetRequests(
	                        timesheetDTO.getEmpId(), timesheetDTO.getTeamId(),
	                        timesheetDTO.getFromDate() != null ? timesheetDTO.getFromDate() : "",
	                        timesheetDTO.getToDate() != null ? timesheetDTO.getToDate() : "",
	                        clientFlag);
	            }
	
	            Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
	                if (list.isEmpty()) {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("No timesheets found. List is empty.");
	                    apiLogInfo.setApiResponse("No timesheets found. List is empty.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                } else {
	                    List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
	
	                    list.forEach((object) -> {
	                        TimesheetDTO dto = buildTimesheetDTOFromObjectArray(object);
	                        
	                        Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
	                        if (timesheetId != null) {
	                            List<TimesheetDocumentDetailsDTO> details = 
	                                    timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
	                            for (TimesheetDocumentDetailsDTO doc : details) {
	                                if (Boolean.TRUE.equals(doc.getFinalFlag())) {
	                                    dto.setApprovedDocument(doc.getDocId());
	                                }
	                                if (Boolean.FALSE.equals(doc.getFinalFlag())) {
	                                    dto.setFilledDocument(doc.getDocId());
	                                }
	                            }
	                        }
	                        
	                        dtoList.add(dto);
	                    });
	
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse(dtoList);
	                    apiLogInfo.setApiResponse("dtoList : " + dtoList);
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                }
	            }, () -> {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No timesheets found. List is null.");
	                apiLogInfo.setApiResponse("No timesheets found. List is null.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            });
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Counts timesheet requests for manager's reportees.
	     * 
	     * @param timesheetDTO Contains managerId
	     * @return ServiceResponse with count
	     */
	    public ServiceResponse countMyReporteesTimesheetRequests(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("view_all_team_requests");
	        apiLogInfo.setApiUrl("/api/countMyReporteesTimesheetRequests");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("managerId : " + timesheetDTO.getManagerId());
	        
	        try {
	            Employee employeeData = employeeRepository.findByEmpId(timesheetDTO.getManagerId());
	            Long applicationCount = timesheetsRepository.countMyReporteesTimesheetRequestsOLD(
	                    timesheetDTO.getManagerId(), employeeData.getDateOfJoining());
	
	            if (applicationCount == 0) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No timesheet request(s) found.");
	                apiLogInfo.setApiResponse("No timesheet request(s) found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            } else {
	                timesheetDTO = new TimesheetDTO();
	                timesheetDTO.setApplicationCount(applicationCount);
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(timesheetDTO);
	                apiLogInfo.setApiResponse("timesheetDTO : " + timesheetDTO);
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Updates timesheet request status (approve/reject).
	     * 
	     * @param timesheetDTO Contains timesheetId, status, and other approval details
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse updateTimesheetRequestById(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("update_request_status");
	        apiLogInfo.setApiUrl("/api/updateTimesheetRequestById");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("timeSheetId : " + timesheetDTO.getTimesheetId());
	        
	        try {
	            Optional<Timesheet> timesheetobject = timesheetsRepository.findById(timesheetDTO.getTimesheetId());
	            
	            if (timesheetobject.isPresent()) {
	                Timesheet timesheet = timesheetobject.get();
	                Timestamp createdOnTimestamp = timesheet.getCommonProperty().getCreatedOn();
	                
	                if (createdOnTimestamp != null) {
	                    LocalDateTime createdOnLDT = createdOnTimestamp.toLocalDateTime();
	                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	                    String formattedCreatedOn = createdOnLDT.format(formatter);
	                    timesheetDTO.setCreatedOn(formattedCreatedOn);
	                } else {
	                    timesheetDTO.setCreatedOn(null);
	                }
	            }
	
	            // Handle client-side document approval if applicable
	            if (timesheetDTO.getClientSideId() != null) {
	                ServiceResponse response1 = bulkTimesheetDocumentApproval(
	                        Collections.singletonList(timesheetDTO));
	                if (ServiceResponse.STATUS_FAIL.equals(response1.getServiceStatus())
	                        || ServiceResponse.SOMETHING_WENT_WRONG.equals(response1.getServiceStatus())) {
	                    throw new RuntimeException("Timesheet Document Failed To Approve");
	                }
	            }
	            
	            timesheetobject.ifPresentOrElse((timesheet) -> {
	                timesheet.setStatus(timesheetDTO.getStatus());
	                timesheet.setTimesheetStatusUpdatedBy(timesheetDTO.getTimesheetStatusUpdatedBy());
	                timesheet.setRemarks(timesheetDTO.getRejectReason());
	                timesheet.setRejectionId(timesheetDTO.getRejectionId() != null 
	                        ? timesheetDTO.getRejectionId().longValue() 
	                        : null);
	                Timesheet updatedTimesheet = timesheetsRepository.save(timesheet);
	
	                ServiceResponse response2 = bulkTimesheetDocumentApprovalLogs(
	                        Collections.singletonList(timesheetDTO));
	                if (ServiceResponse.STATUS_FAIL.equals(response2.getServiceStatus())
	                        || ServiceResponse.SOMETHING_WENT_WRONG.equals(response2.getServiceStatus())) {
	                    throw new RuntimeException("Timesheet Document Failed To Approve");
	                }
	                
	                if (updatedTimesheet.getEmpId() != null) {
	                    if (updatedTimesheet.getStatus().equals("Approved")) {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("Timesheet status Approved.");
	                        apiLogInfo.setApiResponse("Timesheet status Approved.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("Timesheet status Rejected.");
	                        apiLogInfo.setApiResponse("Timesheet status Rejected.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	
	                        try {
	                            mailService.sendMailWithCC(
	                                    timesheetDTO.getEmail(), timesheetDTO.getManagerEmail(),
	                                    "Regarding Timesheet Request Rejection",
	                                    buildRejectionEmailBody(timesheetDTO));
	                        } catch (Exception e) {
	                            e.printStackTrace();
	                            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	                            response.setServiceResponse("Something Went Wrong.");
	                            response.setServiceError(e.getMessage());
	                            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                            apiLogInfo.setLogLevel("ERROR");
	                        }
	                    }
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Timesheet status updation failed.");
	                    apiLogInfo.setApiResponse("Timesheet status updation failed.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                }
	            }, () -> {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Timesheet not found");
	                apiLogInfo.setApiResponse("Timesheet not found");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            });
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Approves timesheet request for multiple employee/team pairs.
	     * 
	     * @param timesheetDTO Contains pendingApprovalList
	     * @return ServiceResponse
	     */
	    public ServiceResponse approveTimesheetRequest(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("approveTimesheetRequest");
	        apiLogInfo.setApiUrl("/api/approveTimesheetRequest");
	        apiLogInfo.setLogLevel("INFO");
	
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("Approving timesheets for ")
	                  .append(timesheetDTO.getPendingApprovalList().size())
	                  .append(" employee/team pairs.\n");
	
	        List<TimesheetDTO> approvedList = new ArrayList<>();
	
	        try {
	            for (TimesheetDTO entry : timesheetDTO.getPendingApprovalList()) {
	                Long empId = entry.getEmpId();
	                Long teamId = entry.getTeamId();
	
	                logBuilder.append("Processing empId: ").append(empId)
	                          .append(", teamId: ").append(teamId).append("\n");
	
	                List<Object[]> pendingTimesheets = timesheetsRepository.getPendingTimesheetsByEmpAndTeam(empId, teamId);
	
	                if (pendingTimesheets != null && !pendingTimesheets.isEmpty()) {
	                    for (Object[] object : pendingTimesheets) {
	                        TimesheetDTO dto = buildTimesheetDTOFromObjectArray(object);
	                        dto.setStatus(entry.getStatus());
	                        dto.setRejectionId(entry.getRejectionId());
	                        dto.setTimesheetStatusUpdatedBy(entry.getTimesheetStatusUpdatedBy());
	
	                        Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
	                        if (timesheetId != null) {
	                            List<TimesheetDocumentDetailsDTO> details = 
	                                    timesheetDocumentDetailsRepository.findAllDocIdByTimesheetId(timesheetId);
	                            for (TimesheetDocumentDetailsDTO doc : details) {
	                                if (Boolean.TRUE.equals(doc.getFinalFlag())) {
	                                    dto.setApprovedDocument(doc.getDocId());
	                                }
	                                if (Boolean.FALSE.equals(doc.getFinalFlag())) {
	                                    dto.setFilledDocument(doc.getDocId());
	                                }
	                            }
	                        }
	
	                        try {
	                            updateTimesheetRequestById(dto);
	                            approvedList.add(dto);
	                        } catch (Exception e) {
	                            logBuilder.append("Failed to approve timesheetId ").append(timesheetId)
	                                      .append(": ").append(e.getMessage()).append("\n");
	                            e.printStackTrace();
	                        }
	                    }
	                } else {
	                    logBuilder.append("No pending timesheets found for empId: ").append(empId)
	                              .append(", teamId: ").append(teamId).append("\n");
	                }
	            }
	
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Approved timesheets: " + approvedList.size());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            apiLogInfo.setApiResponse("Approved timesheet count: " + approvedList.size());
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Error while approving timesheets.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     	 * Bulk approves timesheet requests.
	         * 
	         * @param timesheetIdList Contains bulkApprovedList
	         * @return ServiceResponse
	         */
	        @Transactional(rollbackFor = Exception.class)
	        public ServiceResponse bulkApproveTimesheetRequest(TimesheetApprovalNewDTO timesheetIdList) {
	            ServiceResponse response = new ServiceResponse();
//	            List<TimesheetApprovalNewDTO> timesheetList = timesheetIdList.getBulkApprovedList();
//	    
//	            try {
//	                Map<Long, TimesheetDTO> dtoMap = timesheetList.stream()
//	                        .collect(Collectors.toMap(TimesheetDTO::getTimesheetId, Function.identity()));
//	    
//	                List<Long> timesheetIds = new ArrayList<>(dtoMap.keySet());
//	                List<Timesheet> timesheets = timesheetsRepository.findAllById(timesheetIds);
//	    
//	                // Update in batch
//	                for (Timesheet timesheet : timesheets) {
//	                    if (timesheet != null) {
//	                        timesheet.setStatus(timesheetIdList.getStatus());
//	                        timesheet.setTimesheetStatusUpdatedBy(timesheetIdList.getUpdatedBy());
//	                        timesheet.setRemarks(timesheetIdList.getRejectReason());
//	                    }
//	                }
//	    
//	                timesheetsRepository.saveAll(timesheets);
//	    
//	                List<TimesheetDTO> withClientSideId = timesheetList == null
//	                        ? Collections.emptyList()
//	                        : timesheetList.stream()
//	                              .filter(t -> t.getClientSideId() != null)
//	                              .collect(Collectors.toList());
//	                
//	                if (!withClientSideId.isEmpty()) {
//	                    ServiceResponse approvalResp = bulkTimesheetDocumentApproval(withClientSideId);
//	                    if (!ServiceResponse.STATUS_SUCCESS.equals(approvalResp.getServiceStatus())) {
//	                        throw new RuntimeException("Bulk Timesheet Document Approval Failed");
//	                    }
//	                }
//	    
//	                ServiceResponse logsResp = bulkTimesheetDocumentApprovalLogs(timesheetList);
//	                if (!ServiceResponse.STATUS_SUCCESS.equals(logsResp.getServiceStatus())) {
//	                    throw new RuntimeException("Bulk Timesheet Document Approval Logs Failed");
//	                }
//	    
//	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	                response.setServiceResponse("Bulk Timesheet Approved Successfully!");
//	    
//	            } catch (Exception e) {
//	                e.printStackTrace();
//	                response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	                response.setServiceResponse("Something went wrong in bulk approval.");
//	                response.setServiceError(e.getMessage());
//	            }
	    
	            return response;
	        }
	
	    /**
	     * Bulk rejects timesheet requests.
	     * 
	     * @param timesheetDTO Contains bulkRejectList
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse bulkRejectTimesheetRequest(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        List<TimesheetDTO> rejectList = timesheetDTO.getBulkRejectList();
	
	        try {
	            Map<Long, TimesheetDTO> dtoMap = rejectList.stream()
	                    .collect(Collectors.toMap(TimesheetDTO::getTimesheetId, Function.identity()));
	
	            List<Long> timesheetIds = new ArrayList<>(dtoMap.keySet());
	            List<Timesheet> timesheets = timesheetsRepository.findAllById(timesheetIds);
	
	            for (Timesheet t : timesheets) {
	                TimesheetDTO dto = dtoMap.get(t.getTimesheetId());
	                if (dto != null) {
	                    t.setStatus(timesheetDTO.getStatus());  // REJECTED
	                    t.setTimesheetStatusUpdatedBy(timesheetDTO.getUpdatedBy());
	                    t.setRemarks(timesheetDTO.getRejectReason());
	                    t.setRejectionId(timesheetDTO.getRejectionId() != null 
	                            ? timesheetDTO.getRejectionId().longValue() 
	                            : null);
	                }
	            }
	
	            timesheetsRepository.saveAll(timesheets);
	
	            // BULK document approval only for records having clientSideId
	            List<TimesheetDTO> withClientSideId =
	                    rejectList == null ? Collections.emptyList()
	                    : rejectList.stream()
	                          .filter(t -> t.getClientSideId() != null)
	                          .collect(Collectors.toList());
	            
	            if (!withClientSideId.isEmpty()) {
	                ServiceResponse approvalResp = bulkTimesheetDocumentApproval(withClientSideId);
	                if (!ServiceResponse.STATUS_SUCCESS.equals(approvalResp.getServiceStatus())) {
	                    throw new RuntimeException("Bulk Timesheet Document Approval Failed");
	                }
	            }
	
	            ServiceResponse logsResp = bulkTimesheetDocumentApprovalLogs(rejectList);
	            if (!ServiceResponse.STATUS_SUCCESS.equals(logsResp.getServiceStatus())) {
	                throw new RuntimeException("Bulk Timesheet Document Approval Logs Failed");
	            }
	
	            // Send rejection emails
	            for (TimesheetDTO dto : rejectList) {
	                try {
	                    mailService.sendMailWithCC(
	                            dto.getEmail(),
	                            dto.getManagerEmail(),
	                            "Regarding Timesheet Request Rejection",
	                            buildRejectionEmailBody(dto));
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                }
	            }
	
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Bulk Timesheets Rejected Successfully!");
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong in bulk rejection.");
	            response.setServiceError(e.getMessage());
	        }
	
	        return response;
	    }
	
	    /**
	     * Revokes an approved timesheet (sets status back to Pending).
	     * 
	     * @param timesheetDTO Contains timesheetId
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse revokeApprovedTimesheet(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("revoke_reportee_timesheet");
	        apiLogInfo.setApiUrl("/api/revokeApprovedTimesheet");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("timeSheetId : " + timesheetDTO.getTimesheetId());
	        
	        try {
	            Optional<Timesheet> timesheetObj = timesheetsRepository.findById(timesheetDTO.getTimesheetId());
	
	            timesheetObj.ifPresentOrElse((timesheetFound) -> {
	                timesheetFound.setStatus("Pending");
	                timesheetsRepository.save(timesheetFound);
	
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse("Timesheet revoked successfully");
	                apiLogInfo.setApiResponse("Timesheet revoked successfully");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }, () -> {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Timesheet not found.");
	                apiLogInfo.setApiResponse("Timesheet not found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            });
	
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Gets all rejection reasons.
	     * 
	     * @return ServiceResponse with list of rejection reasons
	     */
	    public ServiceResponse getRejectionReason() {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("getRejectionReason");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("getRejectionReason");
	        
	        try {
	            Optional<List<TimesheetRejectionReasonsMasterDTO>> detailsOfRejection = 
	                    timesheetRejectionReasonsMasterRepository.getAllRejectionReason();
	            
	            if (!detailsOfRejection.isPresent()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No Reject Reasons Found.");
	                response.setServiceMessage("No Reject Reasons Found.");
	                apiLogInfo.setApiResponse("No Reject Reasons Found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(detailsOfRejection);
	                response.setServiceMessage("Reject reasons fetched successfully.");
	                apiLogInfo.setApiResponse("Reject reasons fetched successfully.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse(e.getMessage());
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        return response;
	    }
	
	    /**
	     * Creates or updates a rejection reason.
	     * 
	     * @param rejectReasonObj Rejection reason DTO
	     * @return ServiceResponse
	     */
	    @Transactional
	    public ServiceResponse setTimesheetRejectReason(TimesheetRejectionReasonsMasterDTO rejectReasonObj) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("setTimesheetRejectReason");
	        apiLogInfo.setApiUrl("/api/setTimesheetRejectReason");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("setTimesheetRejectReason : " + rejectReasonObj.getRejectionReason());
	        
	        try {
	            Optional<TimesheetRejectionReasonsMaster> existingRejectReason = 
	                    timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectReasonObj.getRejectionId());
	            
	            if (existingRejectReason.isEmpty()) {
	                TimesheetRejectionReasonsMaster newRejectReason = new TimesheetRejectionReasonsMaster();
	                newRejectReason.setRejectionReason(rejectReasonObj.getRejectionReason());
	                newRejectReason.setActive(true);
	                newRejectReason.setCreatedBy(rejectReasonObj.getCreatedBy());
	                newRejectReason.setCreatedOn(LocalDateTime.now());
	
	                TimesheetRejectionReasonsMaster newMapping = 
	                        timesheetRejectionReasonsMasterRepository.save(newRejectReason);
	                
	                if (newMapping == null) {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Could not store new reject reason!");
	                    apiLogInfo.setApiResponse("Something went wrong while storing the new reject reason!");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse("New Reject Reason Successfully!");
	                    apiLogInfo.setApiResponse("New Reject Reason created!");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                }
	
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else if (existingRejectReason.isPresent()) {
	                TimesheetRejectionReasonsMaster exstRejectRsn = existingRejectReason.get();
	                exstRejectRsn.setRejectionReason(rejectReasonObj.getRejectionReason());
	                exstRejectRsn.setActive(rejectReasonObj.getActive());
	                exstRejectRsn.setUpdatedBy(rejectReasonObj.getUpdatedBy());
	                exstRejectRsn.setUpdatedOn(LocalDateTime.now());
	
	                TimesheetRejectionReasonsMaster updatedMapping = 
	                        timesheetRejectionReasonsMasterRepository.save(exstRejectRsn);
	
	                if (updatedMapping == null) {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Unable to update the reject reason!");
	                    apiLogInfo.setApiResponse("Failed to update the reject reason!");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse("Updated Successfully!");
	                    apiLogInfo.setApiResponse("Reject reason updated successfully");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                }
	                
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else {
	                apiLogInfo.setApiResponse("Moved to else module");
	                apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	                response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	                response.setServiceResponse("Something Went Wrong.");
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Gets rejection reason by ID.
	     * 
	     * @param rejectionId Rejection reason ID
	     * @return ServiceResponse with rejection reason details
	     */
	    public ServiceResponse getRejectionReasonById(Long rejectionId) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("getRejectionReasonById");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("getRejectionReasonById");
	        
	        try {
	            Optional<TimesheetRejectionReasonsMaster> detailsOfRejection = 
	                    timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectionId);
	            
	            if (detailsOfRejection.isPresent()) {
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(detailsOfRejection);
	                response.setServiceMessage("Reject reasons fetched successfully.");
	                apiLogInfo.setApiResponse("Reject reasons fetched successfully.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("No Reject Reasons Found.");
	                response.setServiceMessage("No Reject Reasons Found.");
	                apiLogInfo.setApiResponse("No Reject Reasons Found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            }
	
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse(e.getMessage());
	            apiLogInfo.setLogLevel("ERROR");
	        }
	
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Updates active status of rejection reason.
	     * 
	     * @param rejectionObj Rejection reason DTO
	     * @return ServiceResponse
	     */
	    @Transactional
	    public ServiceResponse updateActiveByRejectIdId(TimesheetRejectionReasonsMasterDTO rejectionObj) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("updateActiveByRejectIdId");
	        apiLogInfo.setApiUrl("/api/updateActiveByRejectIdId");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("updateActiveByRejectIdId for rejection id: " + rejectionObj.getRejectionId());
	        
	        try {
	            Optional<TimesheetRejectionReasonsMaster> existingRejectReason = 
	                    timesheetRejectionReasonsMasterRepository.findByRejectionId(rejectionObj.getRejectionId());
	            
	            if (existingRejectReason.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Could not find the reject reason!");
	                apiLogInfo.setApiResponse("Something went wrong while fetching the existing reject reason!");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else if (existingRejectReason.isPresent()) {
	                TimesheetRejectionReasonsMaster exstRejectRsn = existingRejectReason.get();
	                exstRejectRsn.setActive(rejectionObj.getActive());
	                exstRejectRsn.setUpdatedBy(rejectionObj.getUpdatedBy());
	                exstRejectRsn.setUpdatedOn(LocalDateTime.now());
	
	                TimesheetRejectionReasonsMaster updatedMapping = 
	                        timesheetRejectionReasonsMasterRepository.save(exstRejectRsn);
	
	                if (updatedMapping == null) {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Unable to update the reject reason!");
	                    apiLogInfo.setApiResponse("Failed to update the reject reason!");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse("Updated Successfully!");
	                    apiLogInfo.setApiResponse("Reject reason updated successfully");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                }
	                
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else {
	                apiLogInfo.setApiResponse("Moved to else module");
	                apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	                response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	                response.setServiceResponse("Something Went Wrong.");
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something Went Wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	        return response;
	    }
	
	    /**
	     * Utility method to get HOD ID and RM ID for an employee.
	     * 
	     * @param timesheetDTO Contains empId
	     * @return ServiceResponse with HOD and RM IDs
	     */
	    public ServiceResponse utiltyMethodToGetHodIdAndRmId(TimesheetDTO timesheetDTO) {
	        ServiceResponse response = new ServiceResponse();
	        LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("utiltyMethodToGetHodIdAndRmId");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("utiltyMethodToGetHodIdAndRmId");
	        
	        try {
	            Optional<EmployeeDTO> empDetails = 
	                    employeeRepository.findEmployeeReportingManagerIdAndHODIdDetailsByEmpId(timesheetDTO.getEmpId());
	            
	            if (empDetails == null || !empDetails.isPresent()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Employee Details Not Found.");
	                response.setServiceMessage("Employee Details Not Found.");
	                apiLogInfo.setApiResponse("Employee Details Not Found.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            } else {
	                EmployeeDTO dto = empDetails.get();
	                timesheetDTO.setRmId(dto.getReportingManagerId());
	                timesheetDTO.setHodId(dto.getHodId());
	                timesheetDTO.setManagerId(dto.getManagerId());
	                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                response.setServiceResponse(timesheetDTO);
	                response.setServiceMessage("Employee Details Fetched Successfully.");
	                apiLogInfo.setApiResponse("Employee Details Fetched Successfully.");
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong.");
	            response.setServiceError(e.getMessage());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse(e.getMessage());
	            apiLogInfo.setLogLevel("ERROR");
	        }
	        
	        return response;
	    }
	
	    /**
	     * Bulk timesheet document approval.
	     * 
	     * @param timesheetDTOList List of timesheet DTOs
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse bulkTimesheetDocumentApproval(List<TimesheetDTO> timesheetDTOList) {
	        ServiceResponse response = new ServiceResponse();
	        try {
	            // Enrich DTOs with employee hierarchy information
	            for (TimesheetDTO timesheetDTO : timesheetDTOList) {
	                Optional<EmployeeDTO> empDetails =
	                        employeeRepository.findEmployeeReportingManagerIdAndHODIdDetailsByEmpId(
	                                timesheetDTO.getEmpId());
	                if (empDetails.isEmpty()) {
	                    continue;
	                }
	                EmployeeDTO empdto = empDetails.get();
	                timesheetDTO.setRmId(empdto.getReportingManagerId());
	                timesheetDTO.setHodId(empdto.getHodId());
	                timesheetDTO.setManagerId(empdto.getManagerId());
	            }
	            
	            List<Long> timesheetIds =
	                    timesheetDTOList == null ? Collections.emptyList()
	                    : timesheetDTOList.stream()
	                          .map(TimesheetDTO::getTimesheetId)
	                          .filter(Objects::nonNull)
	                          .collect(Collectors.toList());
	            
	            List<TimesheetDocumentApproval> existingApprovals =
	                    timesheetDocumentApprovalRepository.findAllByTimesheetIdIn(timesheetIds);
	            Map<Long, TimesheetDocumentApproval> approvalMap = existingApprovals.stream()
	                    .collect(Collectors.toMap(TimesheetDocumentApproval::getTimesheetId, Function.identity()));
	            
	            List<TimesheetDocumentApproval> approvalsToSave = new ArrayList<>();
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	            
	            for (TimesheetDTO dto : timesheetDTOList) {
	                TimesheetDocumentApproval approval = approvalMap.getOrDefault(
	                        dto.getTimesheetId(), new TimesheetDocumentApproval());
	                approval.setTimesheetId(dto.getTimesheetId());
	                approval.setApproverId(dto.getUpdatedBy());
	                approval.setApprovalStatus("Approved");
	                
	                if (approval.getCreatedOn() == null) {
	                    approval.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn(), formatter));
	                    approval.setCreatedBy(dto.getEmpId());
	                }
	                approval.setUpdatedBy(dto.getUpdatedBy());
	                approval.setUpdatedOn(LocalDateTime.now());
	                approvalsToSave.add(approval);
	            }
	            
	            timesheetDocumentApprovalRepository.saveAll(approvalsToSave);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Timesheet Document Approvals processed successfully.");
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong in document approval.");
	            response.setServiceError(e.getMessage());
	        }
	
	        return response;
	    }
	
	    /**
	     * Bulk timesheet document approval logs.
	     * 
	     * @param timesheetDTOList List of timesheet DTOs
	     * @return ServiceResponse
	     */
	    @Transactional(rollbackFor = Exception.class)
	    public ServiceResponse bulkTimesheetDocumentApprovalLogs(List<TimesheetDTO> timesheetDTOList) {
	        ServiceResponse response = new ServiceResponse();
	
	        try {
	            List<Long> timesheetIds =
	                    timesheetDTOList == null ? Collections.emptyList()
	                    : timesheetDTOList.stream()
	                          .map(TimesheetDTO::getTimesheetId)
	                          .filter(Objects::nonNull)
	                          .collect(Collectors.toList());
	            
	            List<TimesheetDocumentApproval> existingApprovals =
	                    timesheetDocumentApprovalRepository.findAllByTimesheetIdIn(timesheetIds);
	            Map<Long, TimesheetDocumentApproval> approvalMap = existingApprovals.stream()
	                    .collect(Collectors.toMap(TimesheetDocumentApproval::getTimesheetId, Function.identity()));
	            
	            List<TimesheetApprovalAllocationLogs> logsToSave = new ArrayList<>();
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	            
	            for (TimesheetDTO dto : timesheetDTOList) {
	                TimesheetDocumentApproval approval = approvalMap.get(dto.getTimesheetId());
	                TimesheetApprovalAllocationLogs log = new TimesheetApprovalAllocationLogs();
	                log.setTimesheetId(dto.getTimesheetId());
	                if (approval != null) {
	                    log.setPreviousLevelId(approval.getPreviousLevelId());
	                    log.setPreviousApproverId(approval.getPreviousApproverId());
	                }
	                log.setApproverId(dto.getUpdatedBy());
	                log.setApprovalStatus(dto.getStatus());
	                log.setAllocId(dto.getAllocId());
	                log.setCreatedOn(LocalDateTime.parse(dto.getCreatedOn(), formatter));
	                log.setCreatedBy(dto.getEmpId());
	                logsToSave.add(log);
	            }
	            
	            timesheetApprovalAllocationLogsRepository.saveAll(logsToSave);
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Timesheet Approval Logs processed successfully.");
	        } catch (Exception e) {
	            e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            response.setServiceResponse("Something went wrong in approval logs.");
	            response.setServiceError(e.getMessage());
	        }
	        
	        return response;
	    }
	
	    // ========== Helper Methods ==========
	
	    /**
	     * Builds TimesheetDTO from object array returned by repository.
	     */
	    private TimesheetDTO buildTimesheetDTOFromObjectArray(Object[] object) {
	        TimesheetDTO dto = new TimesheetDTO();
	        Long timesheetId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
	        dto.setTimesheetId(timesheetId);
	        dto.setDate(object[1] != null ? object[1].toString() : null);
	        dto.setDayType(object[2] != null ? object[2].toString() : null);
	        dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
	        dto.setDescription(object[4] != null ? object[4].toString() : null);
	        dto.setStatus(object[5] != null ? object[5].toString() : null);
	        dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
	        dto.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
	        
	        if (object[8] != null) {
	            String rawDate = object[8].toString();
	            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]");
	            LocalDateTime createdOn = LocalDateTime.parse(rawDate, inputFormatter);
	            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	            String formattedCreatedOn = createdOn.format(outputFormatter);
	            dto.setCreatedOn(formattedCreatedOn);
	        } else {
	            dto.setCreatedOn(null);
	        }
	        
	        dto.setEmployeementId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
	        dto.setTotalTime(object[10] != null ? Float.parseFloat(object[10].toString()) : null);
	        dto.setEmail(object[11] != null ? object[11].toString() : null);
	        dto.setOfficeInTime(object[12] != null ? object[12].toString() : null);
	        dto.setOfficeOutTime(object[13] != null ? object[13].toString() : null);
	        dto.setTotalWorkingOfficeHours(object[14] != null ? object[14].toString() : null);
	        dto.setIsNightShift(object[15] != null ? object[15].toString() : null);
	        dto.setIsConsultant(object[17] != null ? object[17].toString() : null);
	        dto.setIsApprenticeship(object[18] != null ? object[18].toString() : null);
	        dto.setEmpId(object[19] != null ? Long.parseLong(object[19].toString()) : null);
	        dto.setIsApmosysProduct(object[29] != null ? object[29].toString() : null);
	        
	        String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
	        String isApmosysProduct = dto.getIsApmosysProduct();
	        if (employmentId != null) {
	            if ("true".equalsIgnoreCase(isApmosysProduct)) {
	                dto.setEmploymentIdAcToET("AP-" + employmentId);
	            } else {
	                dto.setEmploymentIdAcToET("A-" + employmentId);
	            }
	        }
	        
	        dto.setClientInTime(object[20] != null ? ((java.sql.Timestamp) object[20]).toLocalDateTime() : null);
	        dto.setClientOutTime(object[21] != null ? ((java.sql.Timestamp) object[21]).toLocalDateTime() : null);
	        dto.setClientSideId(object[22] != null ? object[22].toString() : null);
	        dto.setTotalClientWorkingHours(object[23] != null ? object[23].toString() : null);
	        dto.setProjectId(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
	        dto.setClientApprovalStatus(object[25] != null ? object[25].toString() : null);
	        dto.setHasClientSideId(object[26] != null ? (Boolean) object[26] : null);
	        dto.setEmploymentId(dto.getEmpId() != null ? 
	                employeeRepository.fetchEmploymentIdByEmpId(dto.getEmpId()) : null);
	        dto.setIsShadowTimesheet(object[27] != null ? (Boolean) object[27] : null);
	        dto.setShadowEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
	        
	        return dto;
	    }
	
	    /**
	     * Builds rejection email body.
	     */
	    private String buildRejectionEmailBody(TimesheetDTO dto) {
	        return "Dear " + dto.getEmployeeName() + "," +
	                "<br><br>Your timesheet application has been rejected by " + dto.getManagerName() + "." +
	                "<br><br><b>Timesheet Details:</b>" +
	                "<br>EmpID: " + dto.getEmployeementId() +
	                "<br>Name: " + dto.getEmployeeName() +
	                "<br>Date: " + dto.getDate() +
	                "<br>Day Type: " + dto.getDayType() +
	                "<br>Total Working Hours: " + dto.getTotalWorkingOfficeHours() + " (hrs)" +
	                "<br><br><b>Rejection Reason:</b> " + dto.getRejectReason();
	    }
	    
	    private ServiceResponse fail(ServiceResponse response, LogDTO apiLogInfo, String message, StringBuilder logBuilder) { 
			
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);  
			response.setServiceResponse(message); 
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        
			logService.logMyInfo(httpRequest, apiLogInfo);
			return response; 
			
		}
	    
	    // public ServiceResponse getMyReporteesTimesheetRequests(GetMyReporteesTimesheetRequestsPayload payload) {
	
	    //     ServiceResponse response = new ServiceResponse();
	        
	    //     LogDTO apiLogInfo = new LogDTO();
	    //     apiLogInfo.setTabName("Home Tab");
	    //     apiLogInfo.setFeatureName("team_timesheets");
	    //     apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
	    //     apiLogInfo.setApiUrl("/api/timesheets/reportees");
	    //     apiLogInfo.setLogLevel("INFO");
	
	    //     StringBuilder logBuilder = new StringBuilder("Home / Team Requests / View Pending Timesheets Requests");
	
	    //     try {
	
	    //         if (payload == null || payload.getEmpId() == null) {
	    //             logBuilder.append(" | Invalid payload or empId is null");
	    //             return fail(response, apiLogInfo,
	    //                     "Employee information is required to fetch timesheet requests",
	    //                     logBuilder);
	    //         }
	
	    //         boolean clientFilter = Boolean.TRUE.equals(payload.getClientFilter());
	
	    //         logBuilder.append(" | ManagerId=").append(payload.getEmpId());
	    //         logBuilder.append(" | ClientFilter=").append(clientFilter);
	        
	    //         /* ---------- Pagination ---------- */
	    //         Pageable pageable = PageRequest.of(
	    //                 payload.getPage(),
	    //                 payload.getSize()
	    //         );
	
	    //         Page<GetReporteesTimesheetReqFlatDTO> pageResult =
	    //         	    employeeTimesheetsNewRepository.getMyReporteesTimesheetRequests(
	    //         	        payload.getEmpId(),
	    //         	        clientFilter,
	
	    //         	        payload.getEmploymentId(),
	    //         	        payload.getEmployeeName(),
	    //         	        payload.getDayType(),
	    //         	        payload.getProjectName(),
	    //         	        payload.getClientName(),
	    //         	        payload.getClientLocation(),
	    //         	        payload.getPoNo(),
	    //         	        payload.getShadowEmpName(),
	    //         	        payload.getTeamName(),
	    //         	        payload.getActivity(),
	    //         	        payload.getDate(),
	            	        
	    //         	        payload.getSearch(),
	            	        
	    //         	        payload.getSortBy(),
	    //         	        payload.getSortDir(),
	    //         	        pageable
	    //         	    );
	
	    //         if (pageResult == null || pageResult.isEmpty()) {
	    //             response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    //             response.setServiceResponse(Collections.emptyList());
	        		
	    //             logBuilder.append(" | No pending requests for timesheet approval! ");
	                
	    //     		apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	    //             apiLogInfo.setApiRequest(logBuilder.toString());
	                
	    //         }
	
	    //         /* ---------- Mapping ---------- */
	    //         List<GetReporteesTimesheetReqDTO> mapped = timesheetMapper.map(pageResult.getContent());
	            
	    //         /* ---------- Response ---------- */
	    //         Map<String, Object> finalResponse = new HashMap<>();
	    //         finalResponse.put("content", mapped);
	    //         finalResponse.put("page", pageResult.getNumber());
	
	    //         /* ---------- Success ---------- */
	    //         response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	    //         response.setServiceResponse(finalResponse);
	
	    //         logBuilder.append(" | Records=").append(finalResponse.size());
	            
	    // 		apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	
	
	    //     } catch (Exception ex) {
	
	    //         response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	    //         response.setServiceResponse("Failed to fetch timesheet requests");
	    //         response.setServiceError(ex.getMessage());
	
	    //         apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	    //         apiLogInfo.setApiResponse(ex.getMessage());
	    //         apiLogInfo.setLogLevel("ERROR");
	
	    //         logBuilder.append(" | Exception=").append(ex.getMessage());
	    //         apiLogInfo.setApiRequest(logBuilder.toString());
	    //         logService.logMyInfo(httpRequest, apiLogInfo);
	            
	    //     }
	    //     return response;
	    // }    
	
	
	    public ServiceResponse getMyReporteesTimesheetRequests(GetMyReporteesTimesheetRequestsPayload payload) {
	
	    ServiceResponse response = new ServiceResponse();
	
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setTabName("Timesheet Tab/Home Tab");
	    apiLogInfo.setFeatureName("team_timesheets");
	    apiLogInfo.setSubFeatureName("view_my_teams_timesheets_requests");
	    apiLogInfo.setApiUrl("/api/timesheets/reportees");
	    apiLogInfo.setLogLevel("INFO");
	
	    StringBuilder logBuilder = new StringBuilder("Timesheet Tab Or Home Tab/ Team Requests / View Pending Timesheets Requests");
	
	    try {
	
	        if (payload == null || payload.getEmpId() == null) {
	            logBuilder.append(" | Invalid payload or empId is null");
	            return fail(response, apiLogInfo,
	                    "Employee information is required to fetch timesheet requests",
	                    logBuilder);
	        }
	
	        boolean clientFilter = Boolean.TRUE.equals(payload.getClientFilter());
	
	        logBuilder.append(" | ManagerId=").append(payload.getEmpId());
	        logBuilder.append(" | ClientFilter=").append(clientFilter);
	
	        // ------------------ Pageable + Sort ------------------
	        int page = (payload.getPage() != null && payload.getPage() >= 0) ? payload.getPage() : 0;
	        int size = (payload.getSize() != null && payload.getSize() > 0) ? payload.getSize() : 10;
	
	        String sortBy = payload.getSortBy();
	        String sortDir = payload.getSortDir();
	
	        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
	
	        String sortExpr;
	        if (sortBy == null || sortBy.isBlank()) {
	            sortExpr = "createdOn";             
	        } else if ("employeeName".equals(sortBy)) {
	            sortExpr = "e.name";                
	        } else if ("employmentId".equals(sortBy)) {
	            sortExpr = "e.employeementId";      
	        } else if ("dayType".equals(sortBy)) {
	            sortExpr = "dtmn.dayType";          
	        } else if ("date".equals(sortBy)) {
	            sortExpr = "date";                  
	        } else if ("appliedOn".equals(sortBy)) {
	            sortExpr = "createdOn";             
	        } else if ("projectName".equals(sortBy)) {
	            sortExpr = "p.projectName";         
	        } else if ("clientName".equals(sortBy)) {
	            sortExpr = "c.clientName";          
	        } else if ("teamName".equals(sortBy)) {
	            sortExpr = "t.teamName";            
	        } else {
	            sortExpr = "createdOn";             
	        }
	
	        Pageable pageable = PageRequest.of(page, size, JpaSort.unsafe(dir, sortExpr));
	      
	        Page<GetReporteesTimesheetReqFlatDTO> pageResult =
	                employeeTimesheetsNewRepository.getMyReporteesTimesheetRequests(
	                        payload.getEmpId(),
	                        clientFilter,
	
	                        payload.getEmploymentId(),
	                        payload.getEmployeeName(),
	                        payload.getDayType(),
	                        payload.getProjectName(),
	                        payload.getClientName(),
	                        payload.getClientLocation(),
	                        payload.getPoNo(),
	                        payload.getShadowEmpName(),
	                        payload.getTeamName(),
	                        payload.getActivity(),
	                        payload.getDate(),
	
	                        payload.getSearch(),
	                        payload.getWorkCheckIn(),
                            payload.getWorkCheckOut(),
                            payload.getLocationCount(),
                            payload.getProjectCount(),
                            payload.getAppliedBy(),
                            payload.getAppliedOn(),
                            payload.getStatus(),
	                        pageable
	                );
	
	        if (pageResult == null || pageResult.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	
	            Map<String, Object> empty = new HashMap<>();
	            empty.put("content", Collections.emptyList());
	            empty.put("page", page);
	            empty.put("size", size);
	            empty.put("totalElements", 0);
	            empty.put("totalPages", 0);
	            empty.put("hasNext", false);
	
	            response.setServiceResponse(empty);
	
	            logBuilder.append(" | No pending requests for timesheet approval!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	
	            return response;
	        }
	
	        // ------------------ Mapping ------------------
	        List<GetReporteesTimesheetReqDTO> mapped = timesheetMapper.map(pageResult.getContent());
	
	        // ------------------ Response ------------------
	        Map<String, Object> finalResponse = new HashMap<>();
	        finalResponse.put("content", mapped);
	        finalResponse.put("page", pageResult.getNumber());
	        finalResponse.put("size", pageResult.getSize());
	        finalResponse.put("totalElements", pageResult.getTotalElements());
	        finalResponse.put("totalPages", pageResult.getTotalPages());
	        finalResponse.put("hasNext", pageResult.hasNext());
	
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(finalResponse);
	
	        logBuilder.append(" | Records=").append(mapped.size());
	        logBuilder.append(" | Page=").append(pageResult.getNumber());
	        logBuilder.append(" | TotalElements=").append(pageResult.getTotalElements());
	
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	
	    } catch (Exception ex) {
	
	    	ex.printStackTrace();
	    	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Failed to fetch timesheet requests");
	        response.setServiceError(ex.getMessage());
	
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse(ex.getMessage());
	        apiLogInfo.setLogLevel("ERROR");
	
	        logBuilder.append(" | Exception=").append(ex.getMessage());
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);
	    }
	
	    return response;
	}
	    
	    
	  
	//  New API
public ServiceResponse getMyReporteesTimesheetRequestsNew(GetMyReporteesTimesheetRequestsPayload payload) {

    ServiceResponse response = new ServiceResponse();

    try {

        // ---------- VALIDATION ----------
        if (payload == null || payload.getEmpId() == null) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Employee information is required");
            return response;
        }

        boolean clientFilter = Boolean.TRUE.equals(payload.getClientFilter());

        // ---------- SAFE PAGE ----------
        int page = (payload.getPage() != null && payload.getPage() >= 0)
                ? payload.getPage()
                : 0;

        int size = (payload.getSize() != null && payload.getSize() > 0)
                ? payload.getSize()
                : 10;

        // ---------- SORT ----------
        String sortBy = payload.getSortBy();
        String sortDir = payload.getSortDir();

        Sort.Direction direction =
                "ASC".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        String sortExpr;

        if (sortBy == null || sortBy.isBlank()) {
            sortExpr = "createdOn";
        } else if ("employeeName".equals(sortBy)) {
            sortExpr = "e.name";
        } else if ("employmentId".equals(sortBy)) {
            sortExpr = "e.employeementId";
        } else if ("dayType".equals(sortBy)) {
            sortExpr = "dtmn.dayType";
        } else if ("date".equals(sortBy)) {
            sortExpr = "date";
        } else if ("appliedOn".equals(sortBy)) {
            sortExpr = "createdOn";
        } else if ("projectName".equals(sortBy)) {
            sortExpr = "p.projectName";
        } else if ("clientName".equals(sortBy)) {
            sortExpr = "c.clientName";
        } else if ("teamName".equals(sortBy)) {
            sortExpr = "t.teamName";
        } else {
            sortExpr = "createdOn";
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                JpaSort.unsafe(direction, sortExpr)
        );

        // ---------- REPO CALL ----------
        Page<GetReporteesTimesheetReqFlatDTO> pageResult =
                employeeTimesheetsNewRepository.getMyReporteesTimesheetRequests(
                        payload.getEmpId(),
                        clientFilter,

                        payload.getEmploymentId(),
                        payload.getEmployeeName(),
                        payload.getDayType(),
                        payload.getProjectName(),
                        payload.getClientName(),
                        payload.getClientLocation(),
                        payload.getPoNo(),
                        payload.getShadowEmpName(),
                        payload.getTeamName(),
                        payload.getActivity(),
                        payload.getDate(),

                        payload.getSearch(),
                        payload.getWorkCheckIn(),
                        payload.getWorkCheckOut(),
                        payload.getLocationCount(),
                        payload.getProjectCount(),
                        payload.getAppliedBy(),
                        payload.getAppliedOn(),
                        payload.getStatus(),
                        pageable
                );

        // ---------- EMPTY ----------
        if (pageResult == null || pageResult.isEmpty()) {

            Map<String, Object> empty = new HashMap<>();
            empty.put("content", Collections.emptyList());
            empty.put("page", page);
            empty.put("size", size);
            empty.put("totalElements", 0);
            empty.put("totalPages", 0);
            empty.put("hasNext", false);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(empty);
            return response;
        }

        // ---------- MAP ----------
        List<GetReporteesTimesheetReqDTO> mapped =
                timesheetMapper.map(pageResult.getContent());

        // ---------- FINAL RESPONSE ----------
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("content", mapped);//
        finalResponse.put("page", pageResult.getNumber());//
        finalResponse.put("size", pageResult.getSize());//
        finalResponse.put("totalElements", pageResult.getTotalElements());
        finalResponse.put("totalPages", pageResult.getTotalPages());//
        finalResponse.put("hasNext", pageResult.hasNext());//

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse(finalResponse);

    } catch (Exception ex) {

        ex.printStackTrace();

        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        response.setServiceResponse("Failed to fetch timesheet requests");
        response.setServiceError(ex.getMessage());
    }

    return response;
}
	}
	
