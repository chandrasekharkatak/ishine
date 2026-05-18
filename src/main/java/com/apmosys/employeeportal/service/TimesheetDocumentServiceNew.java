package com.apmosys.employeeportal.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DocumentResponseDTONew;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.ClientStatusMasterNewRepository;
import com.apmosys.employeeportal.repository.DocMimeTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;

@Service
public class TimesheetDocumentServiceNew {

    @Autowired
    private TimesheetDocumentDetailsNewRepository timesheetDocumentDetailsNewRepository;

    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private FinalDocumentNewRepository finalDocumentNewRepository;

    @Autowired
    private ClientStatusMasterNewRepository clientStatusMasterNewRepository;

    @Autowired
    private DocMimeTypeMasterNewRepository mimeTypeRepository;

    @Autowired
    private DayTypeMasterNewRepository dayTypeMasterNewRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private HttpServletRequest httpRequest;

    @Value("${upload.path}")
    private String storagePath;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "xls", "xlsx");

    /**
     * Handles Document uploads for timesheet
     * 
     * @param newEmpDTO           The timesheet DTO containing document metadata
     * @param newTimesheetCreated The timesheet entity to associate the document
     *                            with
     * @param files               The multipart file to upload
     * @throws IllegalArgumentException if document validation fails
     */
    @Transactional
    public void handleDocumentUpload(
            EmployeeTimesheetsNew empTs,
            Long timesheetId,
            List<MultipartFile> documents, List<TimesheetDocumentDataDTO> documentDataList, boolean isUpdate) {

        if (documents.size() != documentDataList.size() && !isUpdate) {
            throw new IllegalArgumentException("Please ensure all required documents are attached.");
        }

        // Sort: Filled first, then Approved - ensures Filled are processed before
        // Approved
        documentDataList.sort(
                Comparator.comparing(
                        d -> "Filled".equalsIgnoreCase(d.getDocType()) ? 0 : 1));
        List<TimesheetDocumentDataDTO> filledDocs = new ArrayList<>();
        List<TimesheetDocumentDataDTO> approvedDocs = new ArrayList<>();

        for (TimesheetDocumentDataDTO docData : documentDataList) {
            if ("Filled".equalsIgnoreCase(docData.getDocType())) {
                filledDocs.add(docData);
            } else if ("Approved".equalsIgnoreCase(docData.getDocType())) {
                approvedDocs.add(docData);
            }
        }

        // Create file lookup map - docName must match original filename
        Map<String, MultipartFile> fileMap = documents.stream()
                .collect(Collectors.toMap(
                        MultipartFile::getOriginalFilename,
                        f -> f,
                        (a, b) -> a));

        Map<String, Long> timesheetIdAndProjectIdToFinalDocMap = new HashMap<>();

        for (TimesheetDocumentDataDTO approvedData : approvedDocs) {
            MultipartFile matchedFile = fileMap.get(approvedData.getUniqueIdentifier());
            if (matchedFile == null && !isUpdate) {
                throw new IllegalArgumentException(
                        "Document is missing: " + approvedData.getDocName());
            }
            String storedFileName = matchedFile != null ? uploadFile(matchedFile, approvedData.getUniqueIdentifier()) : approvedData.getUniqueIdentifier();

            if (approvedData.getDocId() != null) {
                // REPLACE: FinalDocumentNew may be shared by multiple TimesheetDocumentDetailsNew (same/different timesheets).
                // a) If only one row points to this finalDocId and it's the one we're updating → update in place.
                // b) If multiple rows point to it → create new FinalDocumentNew and link only current timesheet's row.
                Long finalDocId = approvedData.getDocId();
                List<TimesheetDocumentDetailsNew> referrers = timesheetDocumentDetailsNewRepository
                        .getDocsByBulkApproverDocId(finalDocId);
                boolean singleReferrerAndThisTimesheet = referrers != null && referrers.size() == 1
                        && Objects.equals(referrers.get(0).getTimesheetId(), timesheetId)
                        && Objects.equals(referrers.get(0).getProjectId(), approvedData.getProjectId());

                if (singleReferrerAndThisTimesheet) {
                    Optional<FinalDocumentNew> existingOpt = finalDocumentNewRepository.findById(finalDocId);
                    if (existingOpt.isPresent()) {
                        FinalDocumentNew existing = existingOpt.get();
                        String oldFileUrl = existing.getFileUrl();
                        existing.setFileUrl(storedFileName);
                        existing.setDocName(approvedData.getDocName());
                        existing.setMimeTypeId(getMimeTypeId(approvedData.getDocName(), approvedData.getUniqueIdentifier()));
                        existing.setUpdatedBy(empTs.getUpdatedBy());
                        finalDocumentNewRepository.save(existing);
                        if (oldFileUrl != null && !oldFileUrl.equals(storedFileName)) {
                            deleteFile(oldFileUrl);
                        }
                        timesheetIdAndProjectIdToFinalDocMap.put(empTs.getTimesheetId() + "_" + approvedData.getProjectId(), existing.getFinalDocId());
                        continue;
                    }
                }

                // Shared by multiple timesheets (or other edge case): create new FinalDocumentNew, link only this timesheet
                FinalDocumentNew newFinalDoc = new FinalDocumentNew();
                newFinalDoc.setProjectId(approvedData.getProjectId());
                newFinalDoc.setFileUrl(storedFileName);
                newFinalDoc.setCreatedBy(empTs.getCreatedBy());
                newFinalDoc.setUpdatedBy(empTs.getUpdatedBy());
                newFinalDoc.setDocName(approvedData.getDocName());
                newFinalDoc.setMimeTypeId(getMimeTypeId(approvedData.getDocName(), approvedData.getUniqueIdentifier()));
                newFinalDoc = finalDocumentNewRepository.save(newFinalDoc);
                timesheetIdAndProjectIdToFinalDocMap.put(empTs.getTimesheetId() + "_" + approvedData.getProjectId(), newFinalDoc.getFinalDocId());
                continue;
            }

            // UPDATE existing FinalDocumentNew when project was moved (e.g. location-1 → location-2) to avoid duplicate row and file on server.
            // Find existing doc row for (timesheetId, projectId) that already has bulkApprovedDocId; update that FinalDocumentNew in place.
            List<TimesheetDocumentDetailsNew> existingForProjectApproved = timesheetDocumentDetailsNewRepository
                    .findByTimesheetIdAndProjectIdAndActive(timesheetId, approvedData.getProjectId());
            TimesheetDocumentDetailsNew rowWithFinal = existingForProjectApproved == null ? null : existingForProjectApproved.stream()
                    .filter(d -> d.getBulkApprovedDocId() != null)
                    .findFirst()
                    .orElse(null);
            if (rowWithFinal != null) {
                Long existingFinalDocId = rowWithFinal.getBulkApprovedDocId();
                Optional<FinalDocumentNew> existingFinalOpt = finalDocumentNewRepository.findById(existingFinalDocId);
                if (existingFinalOpt.isPresent()) {
                    FinalDocumentNew existingFinal = existingFinalOpt.get();
                    String oldFileUrl = existingFinal.getFileUrl();
                    existingFinal.setFileUrl(storedFileName);
                    existingFinal.setDocName(approvedData.getDocName());
                    existingFinal.setMimeTypeId(getMimeTypeId(approvedData.getDocName(), approvedData.getUniqueIdentifier()));
                    existingFinal.setUpdatedBy(empTs.getUpdatedBy());
                    finalDocumentNewRepository.save(existingFinal);
                    if (oldFileUrl != null && !oldFileUrl.equals(storedFileName)) {
                        deleteFile(oldFileUrl);
                    }
                    timesheetIdAndProjectIdToFinalDocMap.put(empTs.getTimesheetId() + "_" + approvedData.getProjectId(), existingFinalDocId);
                    continue;
                }
            }

            // CREATE: new Approved row (no existing docId and no existing FinalDocumentNew for this timesheet+project)
            FinalDocumentNew finalDocumentNew = new FinalDocumentNew();
            finalDocumentNew.setProjectId(approvedData.getProjectId());
            finalDocumentNew.setFileUrl(storedFileName);
            finalDocumentNew.setCreatedBy(empTs.getCreatedBy());
            finalDocumentNew.setUpdatedBy(empTs.getUpdatedBy());
            finalDocumentNew.setDocName(approvedData.getDocName());
            finalDocumentNew.setMimeTypeId(getMimeTypeId(approvedData.getDocName(), approvedData.getUniqueIdentifier()));
            finalDocumentNew = finalDocumentNewRepository.save(finalDocumentNew);
            timesheetIdAndProjectIdToFinalDocMap.put(empTs.getTimesheetId() + "_" + approvedData.getProjectId(), finalDocumentNew.getFinalDocId());
        }

        for (TimesheetDocumentDataDTO filledData : filledDocs) {
            MultipartFile matchedFile = fileMap.get(filledData.getUniqueIdentifier());
            if (matchedFile == null && !isUpdate) {
                throw new IllegalArgumentException(
                        "Filled document is missing. Please upload the required document.");
            }
            String storedFileName = matchedFile!= null ? uploadFile(matchedFile, filledData.getUniqueIdentifier()) : filledData.getUniqueIdentifier();

            if (filledData.getDocId() != null) {
                // REPLACE: update existing Filled row so the same document slot shows the new file
                Optional<TimesheetDocumentDetailsNew> existingOpt = timesheetDocumentDetailsNewRepository.findById(filledData.getDocId());
                if (existingOpt.isPresent()) {
                    TimesheetDocumentDetailsNew existing = existingOpt.get();
                    String oldFileUrl = existing.getFileUrl();
                    existing.setFileUrl(storedFileName);
                    existing.setDocName(filledData.getDocName());
                    existing.setMimeTypeId(getMimeTypeId(filledData.getDocName(), filledData.getUniqueIdentifier()));
                    existing.setUpdatedBy(empTs.getUpdatedBy());
                    if (timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()) != null) {
                        existing.setBulkApprovedDocId(timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()));
                        existing.setClientApprovalStatusId(2);
                        existing.setFinalFlag(true);
                    }
                    timesheetDocumentDetailsNewRepository.save(existing);
                    if (oldFileUrl != null && !oldFileUrl.equals(storedFileName)) {
                        deleteFile(oldFileUrl);
                    }
                    continue;
                }
            }

            // UPDATE existing row when project was moved (e.g. location-1 → location-2) to avoid duplicate row and file on server.
            // Find by (timesheetId, projectId); document is stored per timesheet+project, not per location.
            List<TimesheetDocumentDetailsNew> existingForProject = timesheetDocumentDetailsNewRepository
                    .findByTimesheetIdAndProjectIdAndActive(timesheetId, filledData.getProjectId());
            if (existingForProject != null && !existingForProject.isEmpty()) {
                // Use the filled-doc row (first one, or the one with finalFlag=false for the uploaded file slot)
                TimesheetDocumentDetailsNew existing = existingForProject.stream()
                        .filter(d -> Boolean.FALSE.equals(d.getFinalFlag()) || d.getBulkApprovedDocId() == null)
                        .findFirst()
                        .orElse(existingForProject.get(0));
                String oldFileUrl = existing.getFileUrl();
                existing.setFileUrl(storedFileName);
                existing.setDocName(filledData.getDocName());
                existing.setMimeTypeId(getMimeTypeId(filledData.getDocName(), filledData.getUniqueIdentifier()));
                existing.setUpdatedBy(empTs.getUpdatedBy());
                if (timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()) != null) {
                    existing.setBulkApprovedDocId(timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()));
                    existing.setClientApprovalStatusId(2);
                    existing.setFinalFlag(true);
                } else {
                    existing.setClientApprovalStatusId(1);
                    existing.setFinalFlag(false);
                }
                timesheetDocumentDetailsNewRepository.save(existing);
                if (oldFileUrl != null && !oldFileUrl.equals(storedFileName)) {
                    deleteFile(oldFileUrl);
                }
                continue;
            }

            // CREATE: new Filled row (no existing row for this timesheet+project)
            TimesheetDocumentDetailsNew doc = new TimesheetDocumentDetailsNew();
            doc.setTimesheetId(timesheetId);
            doc.setProjectId(filledData.getProjectId());
            doc.setFileUrl(storedFileName);
            doc.setDocName(filledData.getDocName());
            doc.setMimeTypeId(getMimeTypeId(filledData.getDocName(), filledData.getUniqueIdentifier()));
            doc.setCreatedBy(empTs.getCreatedBy());
            doc.setActive(true);
            doc.setUpdatedBy(empTs.getUpdatedBy());
            doc.setClientApprovalStatusId(1);
            doc.setFinalFlag(false);
            if (timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()) != null) {
                doc.setBulkApprovedDocId(timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()));
                doc.setClientApprovalStatusId(2);
                doc.setFinalFlag(true);
            }
            timesheetDocumentDetailsNewRepository.save(doc);
        }
    }

    /**
     * Replaces all temporary files with a final file for a date range.
     * 
     * This method:
     * 1. Validates input parameters
     * 2. Fetches temporary documents for the employee and date range
     * 3. Filters valid temporary documents (Working/Non-Working day types, not
     * already final)
     * 4. Creates a single FinalDocument entity
     * 5. Updates all valid temporary documents to link to the FinalDocument via
     * bulkApprovedDocId
     * 6. Updates timesheet statuses to "Pending" and "Approved"
     * 
     * @param file     The final file to replace temporary files with
     * @param fromDate Start date
     * @param toDate   End date
     * @param empId    Employee ID
     * @return ServiceResponse
     * @throws Exception if operation fails
     */
    @Transactional(rollbackFor = Exception.class)
    public ServiceResponse replaceAllTemporaryFileWithFinalFile(
            MultipartFile file, LocalDate fromDate, LocalDate toDate, Long empId) throws Exception {

        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("bulkFinalDocumentUpload");
        apiLogInfo.setApiUrl("/api/v2/timesheet/bulkFinalDocumentUpload");
        apiLogInfo.setLogLevel("INFO");
        apiLogInfo.setEmpId(empId);
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("empId: ").append(empId).append(", fromDate: ").append(fromDate).append(", toDate: ")
                .append(toDate).append(", fileName: ").append(file != null ? file.getOriginalFilename() : null);

        try {
            /*
             * =======================
             * BASIC VALIDATION
             * =======================
             */
            if (empId == null || fromDate == null || toDate == null || file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Required input(s) are missing or file is empty.");
            }

            /*
             * =======================
             * FETCH TEMP DOCUMENTS
             * =======================
             */
            List<TimesheetDocumentDetailsNew> tempDocs = timesheetDocumentDetailsNewRepository
                    .getDocsByEmpAndDateRange(empId, fromDate, toDate);

            if (tempDocs == null || tempDocs.isEmpty()) {
                throw new IllegalStateException("No temporary documents found.");
            }

            /*
             * =======================
             * CACHE TIMESHEETS
             * =======================
             */
            Set<Long> timesheetIds = tempDocs.stream()
                    .map(TimesheetDocumentDetailsNew::getTimesheetId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, EmployeeTimesheetsNew> timesheetMap = timesheetIds.stream()
                    .map(id -> employeeTimesheetsNewRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(EmployeeTimesheetsNew::getTimesheetId, t -> t));

            /*
             * =======================
             * FILTER VALID TEMP DOCS
             * =======================
             */
            List<TimesheetDocumentDetailsNew> validTempDocs = tempDocs.stream()
                    .filter(doc -> {
                        EmployeeTimesheetsNew ts = timesheetMap.get(doc.getTimesheetId());
                        return ts != null
                                && ts.getDayTypeId() != null
                                && (ts.getDayTypeId() == 1 || ts.getDayTypeId() == 3);
                    })
                    .filter(doc -> !(Boolean.TRUE.equals(doc.getFinalFlag())))
                    .collect(Collectors.toList());

            if (validTempDocs.isEmpty()) {
                throw new IllegalStateException("No eligible temporary documents found.");
            }

            /*
             * =======================
             * SAVE FINAL DOCUMENT ONCE
             * =======================
             */
            FinalDocumentNew finalDoc = new FinalDocumentNew();
            finalDoc.setDocName(file.getOriginalFilename());

            finalDoc.setMimeTypeId(getMimeTypeId(file.getContentType(), file.getOriginalFilename()));
            finalDoc.setCreatedOn(LocalDateTime.now());
            FinalDocumentNew savedFinalDoc = finalDocumentNewRepository.save(finalDoc);
            Long finalDocId = savedFinalDoc.getFinalDocId();

            /*
             * =======================
             * UPDATE TEMP DOCS + TIMESHEETS
             * =======================
             */
            for (TimesheetDocumentDetailsNew tempDoc : validTempDocs) {
                // Update temp document status
                tempDoc.setClientApprovalStatusId(2);
                tempDoc.setBulkApprovedDocId(finalDocId);
                tempDoc.setFinalFlag(true);
            }

            /*
             * =======================
             * BULK SAVE TEMP DOCS
             * =======================
             */
            timesheetDocumentDetailsNewRepository.saveAll(validTempDocs);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Final document uploaded and mapped successfully.");
            apiLogInfo.setApiRequest(logBuilder.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            apiLogInfo.setApiResponse("Final document uploaded and mapped successfully.");
            logService.logMyInfo(httpRequest, apiLogInfo);

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiRequest(logBuilder.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse(e.getMessage());
            apiLogInfo.setApiError(e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            logService.logMyInfo(httpRequest, apiLogInfo);
            throw e;
        }

        return response;
    }

    @Transactional
    public ServiceResponse deleteBulkApprovedDocuments(Long bulkApproverDocId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("deleteBulkFinalDocument");
        apiLogInfo.setApiUrl("/api/v2/timesheet/deleteBulkFinalDocument/{bulkApproverDocId}");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("bulkApproverDocId: ").append(bulkApproverDocId);
        try {
            if (bulkApproverDocId == null) {
                throw new IllegalArgumentException("Bulk approver ID is required.");
            }

            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository
                    .getDocsByBulkApproverDocId(bulkApproverDocId);
            if (docs == null || docs.isEmpty()) {
                throw new IllegalArgumentException("No documents found for approval.");
            }

            List<TimesheetDocumentDetailsNew> finalDocs = new ArrayList<>();

            for (TimesheetDocumentDetailsNew doc : docs) {
                doc.setBulkApprovedDocId(null);
                doc.setFinalFlag(false);
                doc.setClientApprovalStatusId(1);
                finalDocs.add(doc);
            }

            timesheetDocumentDetailsNewRepository.saveAll(finalDocs);

            FinalDocumentNew finalDoc = finalDocumentNewRepository.findById(bulkApproverDocId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No final document found for bulk approver ID: " + bulkApproverDocId));

            finalDocumentNewRepository.delete(finalDoc);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Documents deleted successfully.");
            apiLogInfo.setApiRequest(logBuilder.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
            apiLogInfo.setApiResponse("Documents deleted successfully.");
            logService.logMyInfo(httpRequest, apiLogInfo);

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            response.setServiceError(e.getMessage());
            apiLogInfo.setApiRequest(logBuilder.toString());
            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            apiLogInfo.setApiResponse(e.getMessage());
            apiLogInfo.setApiError(e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            logService.logMyInfo(httpRequest, apiLogInfo);
            throw e;
        }

        return response;
    }

    public List<TimesheetDocumentDataDTO> getTimesheetDocumentDataByTimesheetIdAndProjectIds(Long timesheetId, List<Long> projectIds) {
        List<TimesheetDocumentDataDTO> allDocs = timesheetDocumentDetailsNewRepository.getTimesheetDocumentDataByTimesheetIdAndProjectIds(timesheetId, projectIds);

        if(allDocs == null || allDocs.isEmpty()){
            return new ArrayList<>();
        }

        return allDocs;
    }

    /**
     * Fetches document data for a timesheet for update form.
     * Pick one from TimesheetDocumentDetailsNew (Filled) and one from FinalDocumentNew (Approved).
     * Storage: Filled metadata in TimesheetDocumentDetailsNew; Approved metadata in FinalDocumentNew,
     * linked via bulkApprovedDocId on the filled row.
     */
    public List<TimesheetDocumentDataDTO> getTimesheetDocumentDataByTimesheetId(Long timesheetId) {
        if (timesheetId == null) {
            return new ArrayList<>();
        }
        List<TimesheetDocumentDetailsNew> allDocs = timesheetDocumentDetailsNewRepository.findAllByTimesheetId(timesheetId);
        if (allDocs == null || allDocs.isEmpty()) {
            return new ArrayList<>();
        }
        List<TimesheetDocumentDetailsNew> activeDocs = allDocs.stream()
                .filter(doc -> doc.getActive() == null || Boolean.TRUE.equals(doc.getActive()))
                .collect(Collectors.toList());

        List<TimesheetDocumentDataDTO> result = new ArrayList<>();
        List<Long> approvedDocIds = activeDocs.stream()
                .map(TimesheetDocumentDetailsNew::getBulkApprovedDocId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, FinalDocumentNew> finalDocMap = new HashMap<>();
        if (!approvedDocIds.isEmpty()) {
            List<FinalDocumentNew> finalDocs = finalDocumentNewRepository.findAllById(approvedDocIds);
            if (finalDocs != null) {
                finalDocs.forEach(fd -> finalDocMap.put(fd.getFinalDocId(), fd));
            }
        }

        for (TimesheetDocumentDetailsNew doc : activeDocs) {
            result.add(new TimesheetDocumentDataDTO(doc));
            Long bulkApprovedDocId = doc.getBulkApprovedDocId();
            if (bulkApprovedDocId != null) {
                FinalDocumentNew approvedDoc = finalDocMap.get(bulkApprovedDocId);
                if (approvedDoc != null) {
                    result.add(TimesheetDocumentDataDTO.fromFinalDocument(approvedDoc));
                }
            }
        }
        return result;
    }

    /**
     * Clean all document data for a timesheet (Scenario 1 - day type → non-working).
     * Deletes all TimesheetDocumentDetailsNew rows and their filled-doc files (fileUrl).
     * Does NOT touch FinalDocumentNew or its file on server.
     */
    @Transactional
    public void deleteByTimesheetId(Long timesheetId) {
        try {
            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findAllByTimesheetId(timesheetId);
            if (docs == null || docs.isEmpty()) {
                return;
            }
            for (TimesheetDocumentDetailsNew doc : docs) {
                if (doc.getFileUrl() != null && !doc.getFileUrl().isBlank()) {
                    deleteFile(doc.getFileUrl());
                }
            }
            timesheetDocumentDetailsNewRepository.deleteAll(docs);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Uploads multiple files to the storage path
     * 
     * @param files The list of files to upload
     * @throws RuntimeException if file upload fails
     */
    public void uploadFile(List<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                uploadFile(file, file.getOriginalFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload files", e);
        }
    }

    /**
     * Uploads a single file to the storage path
     * 
     * @param file The file to upload
     * @throws RuntimeException if file upload fails
     */
    public String uploadFile(MultipartFile file, String uniqueFileName) {
        try {
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);
            if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
                throw new RuntimeException(
                        "File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
            }

            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            Path filePath = storageDir.resolve(uniqueFileName);
            Files.write(filePath, file.getBytes());
            return uniqueFileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload document. Please try again.", e);
        }
    }

    public Boolean deleteFiles(List<String> fileNames) {
        try {
            for (String fileName : fileNames) {
                deleteFile(fileName);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(storagePath, fileName);

            Files.delete(filePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Views a file from the storage path
     * 
     * @param fileName The name or path of the file to view (relative to storagePath, or absolute)
     * @throws IllegalArgumentException if file name is invalid
     * @throws RuntimeException         if file not found
     */
    public Resource viewFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required");
        }

        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < fileName.length() - 1) {
            String ext = fileName.substring(lastDot + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new IllegalArgumentException("File type not allowed: " + ext);
            }
        }

        try {
            Path basePath = Paths.get(storagePath).normalize();
            Path filePath = basePath.resolve(fileName).normalize();

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                Path fileNameOnly = Paths.get(fileName).getFileName();
                if (fileNameOnly != null && !fileName.equals(fileNameOnly.toString())) {
                    Path altPath = basePath.resolve(fileNameOnly.toString()).normalize();
                    if (Files.exists(altPath) && Files.isReadable(altPath)) {
                        return new UrlResource(altPath.toUri());
                    }
                }
                throw new FileNotFoundException("File not found: " + filePath);
            }

            return new UrlResource(filePath.toUri());

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load file: " + fileName, e);
        }
    }

    /**
     * Retrieves the MIME type ID for a given MIME type or file name
     * 
     * @param mimeType The MIME type to retrieve the ID for
     * @param fileName The file name to retrieve the ID for
     * @throws IllegalArgumentException if MIME type is invalid
     * @throws RuntimeException         if MIME type not found
     */
    public Integer getMimeTypeId(String mimeType, String fileName) {

        if (mimeType != null && !mimeType.isBlank()) {
            String normalizedMime = mimeType.split(";")[0].toLowerCase();

            Integer id = mimeTypeRepository
                    .findByMimeType(normalizedMime)
                    .orElse(null);

            if (id != null) {
                return id;
            }
        }

        String extension = getFileExtension(fileName);

        if (extension == null) {
            throw new IllegalArgumentException("Cannot determine mime type: no extension");
        }

        String derivedMime = extensionToMime(extension);

        if (derivedMime == null) {
            throw new IllegalArgumentException("Unsupported file type: ." + extension);
        }

        return mimeTypeRepository
                .findByMimeType(derivedMime)
                .orElseThrow(() -> new IllegalStateException("MIME type not configured in DB: " + derivedMime));
    }

    /**
     * Retrieves the MIME type for a given file extension
     * 
     * @param ext The file extension to retrieve the MIME type for
     * @return The MIME type for the given file extension
     */
    private String extensionToMime(String ext) {
        switch (ext.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "jpeg":
                return "image/jpeg";
            case "jpg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "xls":
            	return "application/vnd.ms-excel";
            case "xlsx":
            	return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return null;
        }
    }

    /**
     * Retrieves the client approval status ID for a given client approval status
     * 
     * @param clientApprovalStatus The client approval status to retrieve the ID for
     * @return The client approval status ID for the given client approval status
     */
    public Integer getClientApprovalStatusId(String clientApprovalStatus) {
        Optional<Integer> clientStatusId = clientStatusMasterNewRepository
                .findClientStatusIdByClientStatusName(clientApprovalStatus);

        if (clientStatusId.isPresent()) {
            return clientStatusId.get();
        }

        return 1;
    }

    /**
     * Retrieves the file extension for a given filename
     * 
     * @param filename The filename to retrieve the file extension for
     * @return The file extension for the given filename
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Creates a ZIP file from a list of file paths/URLs
     * Optimized for performance with parallel processing and buffered streaming
     * 
     * @param fileUrls List of file paths/URLs to include in the ZIP
     * @param zipFileName Name of the ZIP file to be created
     * @return Byte array of the ZIP file
     * @throws RuntimeException if ZIP creation fails
     */
    public byte[] createZipFromFileUrls(List<String> fileUrls, String zipFileName) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new IllegalArgumentException("No files provided for ZIP creation");
        }

        // Validate and normalize all file paths first
        List<Path> validPaths = fileUrls.parallelStream()
            .map(url -> {
                try {
                    // Handle both absolute and relative paths
                    Path path = Paths.get(url);
                    if (!path.isAbsolute()) {
                        path = Paths.get(storagePath, url);
                    }
                    path = path.normalize();
                    
                    // Security check - prevent directory traversal
                    if (!path.normalize().startsWith(Paths.get(storagePath).normalize())) {
                        System.err.println("Security violation: Attempted to access path outside storage directory: " + url);
                        return null;
                    }
                    
                    return Files.exists(path) && Files.isReadable(path) ? path : null;
                } catch (Exception e) {
                    System.err.println("Invalid file path: " + url + " - " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (validPaths.isEmpty()) {
            throw new IllegalArgumentException("No valid files found to create ZIP");
        }

        // Use try-with-resources for automatic cleanup
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // Set compression level for better performance (0-9, 0=no compression, 9=max)
            zos.setLevel(5); // Balance between speed and compression
            
            // Process files in parallel for better performance
            validPaths.parallelStream().forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    
                    // Create unique entry name to avoid collisions
                    String uniqueName = System.currentTimeMillis() + "_" + 
                                    fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                    
                    // Synchronize on zos for thread-safe ZIP entry creation
                    synchronized (zos) {
                        zos.putNextEntry(new ZipEntry(uniqueName));
                        
                        // Use buffered streaming for large files
                        try (var fis = Files.newInputStream(path)) {
                            byte[] buffer = new byte[8192]; // 8KB buffer
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                zos.write(buffer, 0, bytesRead);
                            }
                        }
                        
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to add file to ZIP: " + path, e);
                }
            });

            zos.finish();
            zos.flush();
            
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ZIP file: " + zipFileName, e);
        }
    }

    // public void compareDocumentStatusAndChangeAccordingly(Long docId,String approvalStatusFromDTO){

    //     Byte finalFlagByDB  = timesheetDocumentDetailsNewRepository.getFinalFlagByDocId(docId);
    //     Boolean finalFlag = finalFlagByDB != null && finalFlagByDB == 1;
    //     // if final flag from db is 2 means it was approved.
    //     // if final flag from db is 1 means it was not pending.
    
    //     // if (!Objects.equals(finalFlagFromDTO, finalFlag)) {

    //         // CASE: Approved -> Pending
    //         if (Boolean.TRUE.equals(finalFlag) && approvalStatusFromDTO.equalsIgnoreCase("Pending")) {
    
                
    //             List<Object[]> result = timesheetDocumentDetailsNewRepository.getFinalDocIdAndFileUrl(docId);
    //             Long finalDocId = result.get(0)[0] == null ? null :((BigInteger) result.get(0)[0]).longValue();
    //             String fileUrl = result.get(0)[1] == null ? null : (String) result.get(0)[1];
    
    //             // 2. Delete file from server
    //             deleteFile(fileUrl);
    
    //             // 3. Delete DB record
    //             finalDocumentNewRepository.deleteById(finalDocId);
    //         }

    //         // }
    //     }

    // public void compareDocumentStatusAndChangeAccordingly(Long docId, String approvalStatusFromDTO) {

    //     try {
    
    //         if (docId == null || approvalStatusFromDTO == null) {
    //             return;
    //         }
    
    //         Byte finalFlagByDB = timesheetDocumentDetailsNewRepository.getFinalFlagByDocId(docId);
    
    //         Boolean finalFlag = finalFlagByDB != null && finalFlagByDB == 1;
    
    //         // CASE: Approved -> Pending
    //         if (Boolean.TRUE.equals(finalFlag) && "Pending".equalsIgnoreCase(approvalStatusFromDTO)) {
    
    //             List<Object[]> result = timesheetDocumentDetailsNewRepository.getFinalDocIdAndFileUrl(docId);
    
    //             if (result == null || result.isEmpty()) {
    //                 return;
    //             }
    
    //             Object[] row = result.get(0);
    
    //             Long finalDocId = row[0] == null ? null : ((BigInteger) row[0]).longValue();
    //             String fileUrl = row[1] == null ? null : row[1].toString();
    
    //             // Delete file
    //             if (fileUrl != null && !fileUrl.isBlank()) {
    //                 deleteFile(fileUrl);
    //             }
    
    //             // Delete DB record
    //             if (finalDocId != null) {
    //                 finalDocumentNewRepository.deleteById(finalDocId);
    //             }
    //         }
    
    //     } catch (Exception ex) {
    //         // Best practice: log instead of ignoring
    //         // log.error("Error while comparing document status for docId: {}", docId, ex);
    //         throw ex;
    //     }
    // }
    @Transactional
    public void compareDocumentStatusAndChangeAccordingly(List<TimesheetDocumentDataDTO> documentDataList) {
    
        try {
    
            if (documentDataList == null || documentDataList.isEmpty()) {
                return;
            }
    
            // Group documents by project
            Map<Integer, List<TimesheetDocumentDataDTO>> projectDocs =
                    documentDataList.stream()
                            .collect(Collectors.groupingBy(TimesheetDocumentDataDTO::getProjectId));
    
            for (Map.Entry<Integer, List<TimesheetDocumentDataDTO>> entry : projectDocs.entrySet()) {
    
                Integer projectId = entry.getKey();
                List<TimesheetDocumentDataDTO> docs = entry.getValue();
    
                // Find filled and approved documents
                TimesheetDocumentDataDTO filledDoc = null;
                TimesheetDocumentDataDTO approvedDoc = null;
    
                for (TimesheetDocumentDataDTO doc : docs) {
    
                    if ("Filled".equalsIgnoreCase(doc.getDocType())) {
                        filledDoc = doc;
                    }
    
                    if ("Approved".equalsIgnoreCase(doc.getDocType())) {
                        approvedDoc = doc;
                    }
                }
    
                // CASE: Project changed from Approved -> Pending
                if (filledDoc != null && approvedDoc == null) {

                    Long filledDocId = filledDoc.getDocId();

                    if (filledDocId == null) {
                        continue;
                    }

                    // Shared-reference rule: do NOT delete FinalDocumentNew or physical file;
                    // another timesheet/user may reference the same document.
                    // Only unlink: set bulkApprovedDocId = null and update flags (finalFlag, clientApprovalStatusId).
                    timesheetDocumentDetailsNewRepository.resetApprovalStatus(filledDocId);
                }
            }
    
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Returns project IDs that have at least one TimesheetDocumentDetailsNew row for this timesheet.
     * Used to delete document rows only when a project is removed from the entire timesheet
     * (same project can appear in multiple locations; we must not delete docs when only one location is removed).
     */
    public Set<Integer> getProjectIdsWithDocumentsForTimesheet(Long timesheetId) {
        if (timesheetId == null) {
            return Set.of();
        }
        List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findAllByTimesheetId(timesheetId);
        if (docs == null || docs.isEmpty()) {
            return Set.of();
        }
        return docs.stream()
                .map(TimesheetDocumentDetailsNew::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Delete document rows for a timesheet+project (Scenarios 2 & 3: location or project removed).
     * Deletes all TimesheetDocumentDetailsNew rows and their filled-doc files (fileUrl).
     * Does NOT touch FinalDocumentNew or its file on server.
     */
    public void deleteDocumentCascade(Long timesheetId, Integer projectId) {
        try {
            if (timesheetId == null || projectId == null) {
                throw new IllegalArgumentException("TimesheetId and ProjectId must not be null");
            }
            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findByTimesheetIdAndProjectIdAndActive(timesheetId, projectId);
            if (docs == null || docs.isEmpty()) {
                return;
            }
            for (TimesheetDocumentDetailsNew doc : docs) {
                if (doc.getFileUrl() != null && !doc.getFileUrl().isBlank()) {
                    deleteFile(doc.getFileUrl());
                }
            }
            timesheetDocumentDetailsNewRepository.deleteAll(docs);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Scenario 4 only: Project status changed from Approved to Filled (Pending).
     * Only updates the final document reference to null (bulkApprovedDocId, finalFlag, clientApprovalStatusId).
     * Does NOT delete any TimesheetDocumentDetailsNew row. Does NOT delete any file. Does NOT touch FinalDocumentNew.
     */
    public void deleteApprovedDocumentsByTimesheetIdAndProjectId(Long timesheetId, Integer projectId) {
        try {
            if (timesheetId == null || projectId == null) {
                return;
            }
            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findByTimesheetIdAndProjectIdAndActive(timesheetId, projectId);
            if (docs == null || docs.isEmpty()) {
                return;
            }
            boolean changed = false;
            for (TimesheetDocumentDetailsNew doc : docs) {
                if (Boolean.TRUE.equals(doc.getFinalFlag())) {
                    doc.setBulkApprovedDocId(null);
                    doc.setFinalFlag(false);
                    doc.setClientApprovalStatusId(1);
                    changed = true;
                }
            }
            if (changed) {
                timesheetDocumentDetailsNewRepository.saveAll(docs);
            }
        } catch (Exception e) {
            throw e;
        }
    }
}

