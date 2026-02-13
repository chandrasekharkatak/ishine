package com.apmosys.employeeportal.service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.ClientStatusMasterNewRepository;
import com.apmosys.employeeportal.repository.DocMimeTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
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
            "pdf", "jpg", "jpeg", "png");

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
            List<MultipartFile> documents, List<TimesheetDocumentDataDTO> documentDataList) {

        if (documents.size() != documentDataList.size()) {
            throw new IllegalArgumentException("Documents count mismatch for timesheet");
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

        // for (TimesheetDocumentDataDTO docData : documentDataList) {
        //     try {
        //         // Verify file exists for this document
        //         MultipartFile matchedFile = fileMap.get(docData.getUniqueIdentifier());
        //         if (matchedFile == null) {
        //             throw new IllegalArgumentException(
        //                     "Document is missing: " + docData.getDocName());
        //         }

        //         if ("Filled".equalsIgnoreCase(docData.getDocType())) {
        //             TimesheetDocumentDetailsNew filledDoc = new TimesheetDocumentDetailsNew();

        //             if (docData.getDocId() != null) {
        //                 TimesheetDocumentDetailsNew existingDoc = timesheetDocumentDetailsNewRepository
        //                         .findById(docData.getDocId()).orElse(null);
        //                 if (existingDoc != null) {
        //                     deleteFile(existingDoc.getFileUrl());
        //                 }
        //                 filledDoc.setDocId(docData.getDocId());
        //             }
        //             filledDoc.setProjectId(docData.getProjectId());
        //             filledDoc.setFileUrl(docData.getUniqueIdentifier());
        //             filledDoc.setDocName(docData.getDocName());
        //             filledDoc.setMimeTypeId(getMimeTypeId(matchedFile.getContentType(), docData.getUniqueIdentifier()));
        //             filledDoc.setClientApprovalStatusId(1);
        //             filledDoc.setActive(true);
        //             filledDoc.setTimesheetId(timesheetId);
        //             filledDoc.setCreatedBy(empTs.getCreatedBy());
        //             filledDoc.setUpdatedBy(empTs.getUpdatedBy());
        //             filledDoc.setFinalFlag(false);
        //             timesheetDocumentDetailsNewRepository.save(filledDoc);

        //         } else if ("Approved".equalsIgnoreCase(docData.getDocType())) {
        //             // timsheetID for safety
        //             List<TimesheetDocumentDetailsNew> existingDetailsNew = timesheetDocumentDetailsNewRepository
        //                     .findByTimesheetIdAndActive(timesheetId, docData.getProjectId());
        //             if (existingDetailsNew == null || existingDetailsNew.isEmpty()) {
        //                 throw new IllegalArgumentException("Document is missing: " + docData.getDocName());
        //             }
        //             FinalDocumentNew finalDocumentNew = new FinalDocumentNew();

        //             if (docData.getBulkApprovedDocId() != null) {
        //                 FinalDocumentNew existingDoc = finalDocumentNewRepository
        //                         .findById(docData.getBulkApprovedDocId()).orElse(null);
        //                 if (existingDoc != null) {
        //                     deleteFile(existingDoc.getFileUrl());
        //                 }
        //                 finalDocumentNew.setFinalDocId(docData.getBulkApprovedDocId());
        //             }
        //             finalDocumentNew.setProjectId(docData.getProjectId());
        //             finalDocumentNew.setFileUrl(docData.getUniqueIdentifier());
        //             finalDocumentNew.setCreatedBy(empTs.getCreatedBy());
        //             finalDocumentNew.setUpdatedBy(empTs.getUpdatedBy());
        //             finalDocumentNew.setDocName(docData.getDocName());
        //             finalDocumentNew.setMimeTypeId(getMimeTypeId(docData.getDocName(), docData.getUniqueIdentifier()));
        //             finalDocumentNew = finalDocumentNewRepository.save(finalDocumentNew);

        //             for (TimesheetDocumentDetailsNew doc : existingDetailsNew) {
        //                 doc.setBulkApprovedDocId(finalDocumentNew.getFinalDocId());
        //                 doc.setFinalFlag(true);
        //                 doc.setClientApprovalStatusId(2);
        //                 timesheetDocumentDetailsNewRepository.save(doc);
        //             }

        //         }
        //         uploadFile(documents);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }

        Map<String, Long> timesheetIdAndProjectIdToFinalDocMap = new HashMap<>();

        for(TimesheetDocumentDataDTO approvedData : approvedDocs) {
            MultipartFile matchedFile = fileMap.get(approvedData.getUniqueIdentifier());
            if (matchedFile == null) {
                throw new IllegalArgumentException(
                        "Document is missing: " + approvedData.getDocName());
            }
            FinalDocumentNew finalDocumentNew = new FinalDocumentNew();
            finalDocumentNew.setFinalDocId(approvedData.getDocId());
            finalDocumentNew.setProjectId(approvedData.getProjectId());
            finalDocumentNew.setFileUrl(approvedData.getUniqueIdentifier());
            finalDocumentNew.setCreatedBy(empTs.getCreatedBy());
            finalDocumentNew.setUpdatedBy(empTs.getUpdatedBy());
            finalDocumentNew.setDocName(approvedData.getDocName());
            finalDocumentNew.setMimeTypeId(getMimeTypeId(approvedData.getDocName(), approvedData.getUniqueIdentifier()));
            finalDocumentNew = finalDocumentNewRepository.save(finalDocumentNew);
            timesheetIdAndProjectIdToFinalDocMap.put(empTs.getTimesheetId() + "_" + approvedData.getProjectId(), finalDocumentNew.getFinalDocId());
        }

        for(TimesheetDocumentDataDTO filledData : filledDocs) {
            MultipartFile matchedFile = fileMap.get(filledData.getUniqueIdentifier());
            if (matchedFile == null) {
                throw new IllegalArgumentException(
                        "Document is missing: " + filledData.getDocName());
            }
            TimesheetDocumentDetailsNew doc = new TimesheetDocumentDetailsNew();
            doc.setTimesheetId(timesheetId);
            doc.setProjectId(filledData.getProjectId());
            doc.setDocId(filledData.getDocId());
            doc.setFileUrl(filledData.getUniqueIdentifier());
            doc.setDocName(filledData.getDocName());
            doc.setMimeTypeId(getMimeTypeId(filledData.getDocName(), filledData.getUniqueIdentifier()));
            doc.setCreatedBy(empTs.getCreatedBy());
            doc.setActive(true);
            doc.setUpdatedBy(empTs.getUpdatedBy());
            doc.setClientApprovalStatusId(1);
            doc.setFinalFlag(false);
            if(timesheetIdAndProjectIdToFinalDocMap.get(timesheetId + "_" + filledData.getProjectId()) != null) {
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

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            response.setServiceError(e.getMessage());
            throw e;
        }

        return response;
    }

    @Transactional
    public ServiceResponse deleteBulkApprovedDocuments(Long bulkApproverDocId) {
        ServiceResponse response = new ServiceResponse();
        try {
            if (bulkApproverDocId == null) {
                throw new IllegalArgumentException("Bulk approver ID is required.");
            }

            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository
                    .getDocsByBulkApproverDocId(bulkApproverDocId);
            if (docs == null || docs.isEmpty()) {
                throw new IllegalArgumentException("No documents found for bulk approver ID: " + bulkApproverDocId);
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

        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(e.getMessage());
            response.setServiceError(e.getMessage());
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

    public List<TimesheetDocumentDataDTO> getTimesheetDocumentDataByTimesheetId(Long timesheetId) {
        List<TimesheetDocumentDetailsNew> allDocs = timesheetDocumentDetailsNewRepository.findByTimesheetIdAndActive(timesheetId);

        if(allDocs == null || allDocs.isEmpty()){
            return new ArrayList<>();
        }

        return allDocs.stream().map(doc -> new TimesheetDocumentDataDTO(doc)).collect(Collectors.toList());
    }

    @Transactional
    public void deleteByTimesheetId(Long timesheetId) {
        try {
            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findAllByTimesheetId(timesheetId);
        
            timesheetDocumentDetailsNewRepository.deleteAll(docs);
            
            List<Long> finalDocIds = docs.stream().map(TimesheetDocumentDetailsNew::getBulkApprovedDocId).collect(Collectors.toList());
            
            List<FinalDocumentNew> finalDocs = finalDocumentNewRepository.findAllById(finalDocIds);

            finalDocumentNewRepository.deleteAll(finalDocs);

            for(TimesheetDocumentDetailsNew doc : docs){
                deleteFile(doc.getFileUrl());
            }

            for(FinalDocumentNew finalDoc : finalDocs){
                deleteFile(finalDoc.getFileUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
    }

    @Transactional
    public void deleteByProjectId(Long projectId) {
        try {
            List<TimesheetDocumentDetailsNew> docs = timesheetDocumentDetailsNewRepository.findAllByProjectId(projectId);
        
            timesheetDocumentDetailsNewRepository.deleteAll(docs);
            
            List<FinalDocumentNew> finalDocs = finalDocumentNewRepository.findByProjectId(projectId);

            finalDocumentNewRepository.deleteAll(finalDocs);

            for(TimesheetDocumentDetailsNew doc : docs){
                deleteFile(doc.getFileUrl());
            }

            for(FinalDocumentNew finalDoc : finalDocs){
                deleteFile(finalDoc.getFileUrl());
            }   
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
            throw new RuntimeException("Failed to upload file", e);
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
     * @param fileName The name of the file to view
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

        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        try {
            Path filePath = Paths.get(storagePath).resolve(fileName).normalize();

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new FileNotFoundException("File not found");
            }

            return new UrlResource(filePath.toUri());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load file", e);
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
}
