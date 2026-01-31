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
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
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
public ServiceResponse bulkApproveTimesheets(TimesheetApprovalNewDTO dto) {

    ServiceResponse response = new ServiceResponse();
    List<EmployeeTimesheetsNew> timesheetDatas = employeeTimesheetsNewRepository.findAllById(dto.getTimesheetIds());
    List<EmployeeTimesheetsNew> rmIdMismatchList = timesheetDatas.stream()
    											.filter(data -> data.getCurrentManagerId() != dto.getRmId())
    											.collect(Collectors.toList());
    if(rmIdMismatchList.size() > 0)
    	throw new IllegalArgumentException("You are not the Approver of some timesheets");
    	
    return response;
}
    
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse bulkRejectTimesheets(TimesheetApprovalNewDTO dto) {

        ServiceResponse response = new ServiceResponse();
        return response;
    }

    
}

