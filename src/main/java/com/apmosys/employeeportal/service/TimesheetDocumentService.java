package com.apmosys.employeeportal.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TimesheetDocumentDetailsDTO;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing timesheet document operations.
 * 
 * This service encapsulates all document-related operations for timesheets:
 * - Document upload and storage
 * - Document retrieval
 * - Document approval/rejection
 * - Bulk document operations
 * 
 * @author Timesheet Refactoring - Phase 3
 */
@Slf4j
@Service
public class TimesheetDocumentService {

    @Autowired
    private TimesheetDocumentDetailsRepository timesheetDocumentDetailsRepository;
    
    @Autowired
    private TimesheetsRepository timesheetsRepository;
    
    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;
    
    @Autowired
    private FinalDocumentRepository finalDocumentRepository;
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private HttpServletRequest httpRequest;

    /**
     * Handles document upload for a timesheet.
     * 
     * @param timesheetDTO The timesheet DTO containing document metadata
     * @param newTimesheetCreated The timesheet entity to associate the document with
     * @param file The multipart file to upload
     * @param isFinal Whether this is a final (approved) document or temporary document
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException if document validation fails
     */
    public void handleDocumentUpload(com.apmosys.employeeportal.dto.TimesheetDTO timesheetDTO, 
                                     Timesheet newTimesheetCreated, 
                                     MultipartFile file,
                                     boolean isFinal) throws IOException {
        log.debug("Handling document upload: timesheetId={}, isFinal={}", 
                newTimesheetCreated.getTimesheetId(), isFinal);

        List<TimesheetDocumentDetailsDTO> documentList = timesheetDTO.getDocumentData().stream()
                .filter(doc -> Boolean.TRUE.equals(doc.getFinalFlag()) == isFinal)
                .collect(Collectors.toList());

        List<TimesheetDocumentDetailsDTO> documentnotFinalList = timesheetDTO.getDocumentData().stream()
                .filter(doc -> Boolean.FALSE.equals(doc.getFinalFlag()) == isFinal)
                .collect(Collectors.toList());

        if (documentList.size() != 1 && documentnotFinalList.size() != 1) {
            throw new IllegalArgumentException("Expected exactly one " + 
                    (isFinal ? "approved" : "unapproved") + " document.");
        }
        
        Long finalDocumentId = null;
        
        if (isFinal) {
            // Create FinalDocument and link to existing TimesheetDocumentDetails
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            FinalDocument finalDocument = new FinalDocument();
            finalDocument.setDocName(fileName);
            finalDocument.setDocData(fileBytes);
            finalDocument.setDocMimeType(contentType);
            finalDocument.setCreatedBy(timesheetDTO.getEmpId());
            finalDocument.setCreatedOn(LocalDateTime.now());
            finalDocument.setUpdatedBy(timesheetDTO.getEmpId());
            finalDocument.setUpdatedOn(LocalDateTime.now());

            finalDocument = finalDocumentRepository.save(finalDocument);
            finalDocumentId = finalDocument.getDocId();
            
            // Update existing TimesheetDocumentDetails to link to FinalDocument
            TimesheetDocumentDetails existedDocDetails = 
                    timesheetDocumentDetailsRepository.findByTimesheetId(newTimesheetCreated.getTimesheetId());
            
            if (existedDocDetails == null) {
                throw new RuntimeException("Document was not saved.");
            }
            
            existedDocDetails.setFinalFlag(isFinal);
            existedDocDetails.setClientApprovalStatus(timesheetDTO.getClientApprovalStatus());
            existedDocDetails.setBulkApprovedDocId(finalDocumentId);
            timesheetDocumentDetailsRepository.save(existedDocDetails);
        } else {
            // Create new TimesheetDocumentDetails for non-final document
            TimesheetDocumentDetailsDTO documentDTO;
            if (documentList.size() != 1) {
                documentDTO = documentnotFinalList.get(0);
            } else {
                documentDTO = documentList.get(0);
            }
            
            TimesheetDocumentDetails docData = addTimesheetDocument(documentDTO, "Create", file);
            docData.setTimesheetId(newTimesheetCreated.getTimesheetId());
            docData.setEmpId(timesheetDTO.getEmpId());
            docData.setCreatedBy(timesheetDTO.getEmpId());

            TimesheetDocumentDetails savedDoc = timesheetDocumentDetailsRepository.save(docData);

            if (savedDoc == null) {
                throw new RuntimeException("Document was not saved.");
            }
        }
        
        log.debug("Document uploaded successfully: isFinal={}, finalDocumentId={}", isFinal, finalDocumentId);
    }

    /**
     * Creates or updates a timesheet document.
     * 
     * @param timesheetDocumentDetailsDTO The document DTO
     * @param oprType Operation type: "Create" or "Update"
     * @param doc The multipart file
     * @return TimesheetDocumentDetails entity
     * @throws IOException if file processing fails
     */
    public TimesheetDocumentDetails addTimesheetDocument(TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO, 
                                                         String oprType, 
                                                         MultipartFile doc) throws IOException {
        TimesheetDocumentDetails data = new TimesheetDocumentDetails();
        
        if ("Create".equalsIgnoreCase(oprType)) {
            timesheetDocumentDetailsDTO.setActive(true);
            timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
        } else if ("Update".equalsIgnoreCase(oprType) && 
                   Boolean.TRUE.equals(timesheetDocumentDetailsDTO.getFinalFlag())) {
            data = timesheetDocumentDetailsRepository.findByDocIdAndFinalFlag(
                    timesheetDocumentDetailsDTO.getDocId(), true);
            if (data == null) {
                data = buildNewDoc(data, timesheetDocumentDetailsDTO, doc);
            }
            timesheetDocumentDetailsDTO.setUpdatedOn(LocalDateTime.now());
        } else if ("Update".equalsIgnoreCase(oprType) && 
                   Boolean.FALSE.equals(timesheetDocumentDetailsDTO.getFinalFlag())) {
            data = timesheetDocumentDetailsRepository.findByDocIdAndFinalFlag(
                    timesheetDocumentDetailsDTO.getDocId(), false);
            if (data == null) {
                data = buildNewDoc(data, timesheetDocumentDetailsDTO, doc);
            }
            timesheetDocumentDetailsDTO.setUpdatedOn(LocalDateTime.now());
        }
        
        if (doc != null) {
            timesheetDocumentDetailsDTO.setDocFile(doc);
        } else {
            throw new DataIntegrityViolationException("No Document found...!!");
        }
        
        data.setDocName(timesheetDocumentDetailsDTO.getDocName());
        data.setDocData(timesheetDocumentDetailsDTO.getDocFile().getBytes());
        data.setTimesheetId(timesheetDocumentDetailsDTO.getTimesheetId());
        data.setEmpId(timesheetDocumentDetailsDTO.getTimesheetId());
        
        if (timesheetDocumentDetailsDTO.getCreatedOn() != null) {
            data.setCreatedOn(timesheetDocumentDetailsDTO.getCreatedOn());
        }
        if (timesheetDocumentDetailsDTO.getCreatedBy() != null) {
            data.setCreatedBy(timesheetDocumentDetailsDTO.getCreatedBy());
        }
        if (timesheetDocumentDetailsDTO.getUpdatedBy() != null) {
            data.setUpdatedBy(timesheetDocumentDetailsDTO.getUpdatedBy());
        }
        if (timesheetDocumentDetailsDTO.getUpdatedOn() != null) {
            data.setUpdatedOn(timesheetDocumentDetailsDTO.getUpdatedOn());
        }
        
        data.setClientApprovalStatus(timesheetDocumentDetailsDTO.getClientApprovalStatus());
        data.setRmApprovalStatus("Pending");
        data.setFinalFlag(timesheetDocumentDetailsDTO.getFinalFlag());
        data.setDocMimeType(timesheetDocumentDetailsDTO.getDocFile().getContentType());
        data.setActive(true);
        data.setBulkApprovedDocId(timesheetDocumentDetailsDTO.getBulkApprovedDocId());
        
        return data;
    }

    /**
     * Builds a new document entity from DTO and file.
     * 
     * @param data The document entity (will be replaced)
     * @param timesheetDocumentDetailsDTO The document DTO
     * @param doc The multipart file
     * @return New TimesheetDocumentDetails entity
     * @throws IOException if file processing fails
     */
    private TimesheetDocumentDetails buildNewDoc(TimesheetDocumentDetails data,
                                                 TimesheetDocumentDetailsDTO timesheetDocumentDetailsDTO,
                                                 MultipartFile doc) throws IOException {
        data = new TimesheetDocumentDetails();
        timesheetDocumentDetailsDTO.setActive(true);
        timesheetDocumentDetailsDTO.setCreatedOn(LocalDateTime.now());
        
        if (doc != null) {
            timesheetDocumentDetailsDTO.setDocFile(doc);
        } else {
            throw new DataIntegrityViolationException("No Document found...!!");
        }
        
        data.setDocName(timesheetDocumentDetailsDTO.getDocName());
        data.setDocData(timesheetDocumentDetailsDTO.getDocFile().getBytes());
        data.setTimesheetId(timesheetDocumentDetailsDTO.getTimesheetId());
        data.setEmpId(timesheetDocumentDetailsDTO.getTimesheetId());
        
        if (timesheetDocumentDetailsDTO.getCreatedOn() != null) {
            data.setCreatedOn(timesheetDocumentDetailsDTO.getCreatedOn());
        }
        if (timesheetDocumentDetailsDTO.getCreatedBy() != null) {
            data.setCreatedBy(timesheetDocumentDetailsDTO.getCreatedBy());
        }
        if (timesheetDocumentDetailsDTO.getUpdatedBy() != null) {
            data.setUpdatedBy(timesheetDocumentDetailsDTO.getUpdatedBy());
        }
        if (timesheetDocumentDetailsDTO.getUpdatedOn() != null) {
            data.setUpdatedOn(timesheetDocumentDetailsDTO.getUpdatedOn());
        }
        
        data.setClientApprovalStatus(timesheetDocumentDetailsDTO.getClientApprovalStatus());
        data.setRmApprovalStatus("Pending");
        data.setFinalFlag(timesheetDocumentDetailsDTO.getFinalFlag());
        data.setDocMimeType(timesheetDocumentDetailsDTO.getDocFile().getContentType());
        data.setActive(true);
        data.setBulkApprovedDocId(timesheetDocumentDetailsDTO.getBulkApprovedDocId());
        
        return data;
    }

    /**
     * Fetches a timesheet document by docId or timesheetId.
     * 
     * @param docId Document ID (optional)
     * @param timesheetId Timesheet ID (optional)
     * @return TimesheetDocumentDetailsDTO with Base64 encoded document data
     * @throws IllegalArgumentException if neither docId nor timesheetId is provided
     */
    public TimesheetDocumentDetailsDTO fetchTimesheetDocument(Long docId, Long timesheetId) {
        TimesheetDocumentDetails entity = new TimesheetDocumentDetails();

        if (docId != null) {
            entity = timesheetDocumentDetailsRepository.findByDocIdAndActive(docId, true);
        } else if (timesheetId != null) {
            entity = timesheetDocumentDetailsRepository.findTopByTimesheetIdAndActive(timesheetId, true);
        } else {
            throw new IllegalArgumentException("Either docId or timesheetId must be provided.");
        }
        
        TimesheetDocumentDetailsDTO dto = new TimesheetDocumentDetailsDTO();
        if (entity != null) {
            dto.setDocId(entity.getDocId());
            dto.setDocName(entity.getDocName());
            dto.setTimesheetId(entity.getTimesheetId());
            dto.setEmpId(entity.getEmpId());
            dto.setCreatedOn(entity.getCreatedOn());
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setUpdatedOn(entity.getUpdatedOn());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setActive(entity.getActive());
            dto.setClientApprovalStatus(entity.getClientApprovalStatus());
            dto.setRmApprovalStatus(entity.getRmApprovalStatus());
            dto.setFinalFlag(entity.getFinalFlag());

            if (entity.getDocData() != null) {
                dto.setDocDataBase64(Base64.getEncoder().encodeToString(entity.getDocData()));
                dto.setMimeType(entity.getDocMimeType());
            }
        } else {
            dto = null;
        }
        
        return dto;
    }

    /**
     * Gets document data by document ID.
     * 
     * @param docId Document ID
     * @return ServiceResponse containing document details
     */
    public ServiceResponse getDocumentDataByDocId(Long docId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getDocumentDataByDocId");
        apiLogInfo.setLogLevel("INFO");
        
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("docId:" + docId + "\n");
        
        try {
            TimesheetDocumentDetails docDetails = timesheetDocumentDetailsRepository.findByDocIdAndActive(docId, true);
            
            if (docDetails == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Document not found...!!");
                response.setServiceMessage("Document not found...!!" + docId);
                
                apiLogInfo.setApiResponse("Document not found...!!" + docId);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                logService.logMyInfo(httpRequest, apiLogInfo);
                return response;
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(docDetails);
                apiLogInfo.setApiResponse("Document details fetched successfully");
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
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Approves or rejects a document.
     * 
     * @param docId Document ID
     * @param approvedOrRejectedBy User ID who is approving/rejecting
     * @param approvalStatus "Approved" or "Rejected"
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse approveOrRejectDocument(Long docId, Long approvedOrRejectedBy, String approvalStatus) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("approveOrRejectDocument");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("approveOrRejectDocument");

        try {
            if (docId == null || approvedOrRejectedBy == null) {
                throw new IllegalArgumentException("Required input(s) are missing.");
            }
            
            TimesheetDocumentDetails timesheetDocumentDetails = 
                    timesheetDocumentDetailsRepository.findByDocIdAndActive(docId, true);
            
            if ("Approved".equalsIgnoreCase(approvalStatus)) {
                timesheetDocumentDetails.setHrApprovalStatus("Approved");
            } else if ("Rejected".equalsIgnoreCase(approvalStatus)) {
                timesheetDocumentDetails.setHrApprovalStatus("Rejected");
            } else {
                throw new IllegalArgumentException("Invalid approval status..!!");
            }
            
            timesheetDocumentDetailsRepository.save(timesheetDocumentDetails);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Document " + approvalStatus + " successfully");
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong.");
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse(e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
        }
        
        apiLogInfo.setApiRequest(logBuilder.toString());
        logService.logMyInfo(httpRequest, apiLogInfo);
        return response;
    }

    /**
     * Replaces all temporary files with a final file for a date range.
     * 
     * This method:
     * 1. Validates input parameters
     * 2. Fetches temporary documents for the employee and date range
     * 3. Filters valid temporary documents (Working/Non-Working day types, not already final)
     * 4. Creates a single FinalDocument entity
     * 5. Updates all valid temporary documents to link to the FinalDocument via bulkApprovedDocId
     * 6. Updates timesheet statuses to "Pending" and "Approved"
     * 
     * @param file The final file to replace temporary files with
     * @param fromDate Start date
     * @param toDate End date
     * @param empId Employee ID
     * @return ServiceResponse
     * @throws Exception if operation fails
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse replaceAllTemporaryFileWithFinalFile(
            MultipartFile file, LocalDate fromDate, LocalDate toDate, Long empId) throws Exception {

        ServiceResponse response = new ServiceResponse();

        try {
            /* =======================
               BASIC VALIDATION
            ======================= */
            if (empId == null || fromDate == null || toDate == null || file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Required input(s) are missing or file is empty.");
            }

            /* =======================
               FETCH TEMP DOCUMENTS
            ======================= */
            List<TimesheetDocumentDetails> tempDocs =
                    timesheetDocumentDetailsRepository.getDocsByEmpAndDateRange(empId, fromDate, toDate);

            if (tempDocs == null || tempDocs.isEmpty()) {
                throw new IllegalStateException("No temporary documents found.");
            }

            /* =======================
               CACHE TIMESHEETS
            ======================= */
            Set<Long> timesheetIds = tempDocs.stream()
                    .map(TimesheetDocumentDetails::getTimesheetId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Timesheet> timesheetMap = timesheetIds.stream()
                    .map(id -> timesheetsRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Timesheet::getTimesheetId, t -> t));

            /* =======================
               FILTER VALID TEMP DOCS
            ======================= */
            List<TimesheetDocumentDetails> validTempDocs =
                    tempDocs.stream()
                            .filter(doc -> {
                                Timesheet ts = timesheetMap.get(doc.getTimesheetId());
                                return ts != null
                                        && ts.getDayType() != null
                                        && ("Working".equalsIgnoreCase(ts.getDayType())
                                        || "Non-Working".equalsIgnoreCase(ts.getDayType()));
                            })
                            .filter(doc ->
                                    !(Boolean.TRUE.equals(doc.getFinalFlag())
                                            && !"Rejected".equalsIgnoreCase(doc.getRmApprovalStatus())
                                            && !"Rejected".equalsIgnoreCase(doc.getHrApprovalStatus()))
                            )
                            .collect(Collectors.toList());

            if (validTempDocs.isEmpty()) {
                throw new IllegalStateException("No eligible temporary documents found.");
            }

            /* =======================
               SAVE FINAL DOCUMENT ONCE
            ======================= */
            FinalDocument finalDoc = new FinalDocument();
            finalDoc.setDocName(file.getOriginalFilename());
            finalDoc.setDocData(file.getBytes());
            finalDoc.setDocMimeType(file.getContentType());
            finalDoc.setCreatedOn(LocalDateTime.now());
            FinalDocument savedFinalDoc = finalDocumentRepository.save(finalDoc);
            Long finalDocId = savedFinalDoc.getDocId();

            /* =======================
               UPDATE TEMP DOCS + TIMESHEETS
            ======================= */
            for (TimesheetDocumentDetails tempDoc : validTempDocs) {
                // Update temp document status
                tempDoc.setClientApprovalStatus("Approved");
                tempDoc.setRmApprovalStatus("Pending");
                tempDoc.setHrApprovalStatus("Pending");
                tempDoc.setBulkApprovedDocId(finalDocId);
                tempDoc.setFinalFlag(true);
                
                // Update timesheet status
                Timesheet ts = timesheetMap.get(tempDoc.getTimesheetId());
                if (ts != null) {
                    ts.setStatus("Pending");
                    ts.setClientApprovalStatus("Approved");
                    timesheetsRepository.save(ts);
                }
            }

            /* =======================
               BULK SAVE TEMP DOCS
            ======================= */
            timesheetDocumentDetailsRepository.saveAll(validTempDocs);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Final document uploaded and mapped successfully.");

        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            response.setServiceError(e.getMessage());
            throw e;
        }

        return response;
    }

    /**
     * Gets all disabled dates for bulk document submit.
     * 
     * @param projectId Project ID
     * @param empId Employee ID
     * @return ServiceResponse containing set of disabled dates
     */
    public ServiceResponse getAllDisabledDateListForBulkDocSubmit(Integer projectId, Long empId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("getAllDisabledDateListForBulkDocSubmit");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("getAllDisabledDateListForBulkDocSubmit");

        try {
            if (projectId == null || empId == null) {
                throw new IllegalArgumentException("Required input(s) are missing.");
            }

            Set<LocalDate> combinedDateSet = new HashSet<>();

            // Always start from 1st of the last month
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            LocalDate firstDayOfLastMonth = lastMonth.atDay(1);

            // End at the end of current month + 3 buffer days
            YearMonth currentMonth = YearMonth.now();
            LocalDate lastDayWithBuffer = currentMonth.atEndOfMonth().plusDays(3);

            // All dates between firstDayOfLastMonth and lastDayWithBuffer
            Set<LocalDate> allDatesInRange = new HashSet<>();
            LocalDate date = firstDayOfLastMonth;
            while (!date.isAfter(lastDayWithBuffer)) {
                allDatesInRange.add(date);
                date = date.plusDays(1);
            }

            // Fetch timesheet filled dates for the range
            Set<LocalDate> allFilledDatesInRange =
                    timesheetsRepository.allTimesheetFilledDatesForDateRange(
                            firstDayOfLastMonth, lastDayWithBuffer, projectId, empId);

            // Unfilled dates = all dates - filled dates
            Set<LocalDate> unfilledDates = new HashSet<>(allDatesInRange);
            if (allFilledDatesInRange != null && !allFilledDatesInRange.isEmpty()) {
                unfilledDates.removeAll(allFilledDatesInRange);
            }
            combinedDateSet.addAll(unfilledDates);

            // Add employee leave dates
            List<EmployeeLeave> empLeaveData =
                    employeeLeaveRepository.findLeavesInCurrentMonth(empId, firstDayOfLastMonth, lastDayWithBuffer);
            if (empLeaveData != null && !empLeaveData.isEmpty()) {
                Set<LocalDate> leaveDates = getAllLeaveDates(empLeaveData);
                if (leaveDates != null && !leaveDates.isEmpty()) {
                    combinedDateSet.addAll(leaveDates);
                }
            }

            // Add timesheet dates that already have both documents (if applicable)
            Set<LocalDate> timesheetDates = timesheetsRepository.findDatesByEmpIdAndProjectId(empId, projectId);
            if (timesheetDates != null && !timesheetDates.isEmpty()) {
                combinedDateSet.addAll(timesheetDates);
            }

            // Prepare response
            if (!combinedDateSet.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(combinedDateSet);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No data found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
     * Helper method to get all leave dates from employee leave list.
     * 
     * @param empLeaveData List of employee leaves
     * @return Set of leave dates
     */
    public Set<LocalDate> getAllLeaveDates(List<EmployeeLeave> empLeaveData) {
        Set<LocalDate> leaveDates = new HashSet<>();

        for (EmployeeLeave leave : empLeaveData) {
            LocalDate start = leave.getFromDate();
            LocalDate end = leave.getToDate();

            while (!start.isAfter(end)) {
                leaveDates.add(start);
                start = start.plusDays(1);
            }
        }

        return leaveDates;
    }
}

