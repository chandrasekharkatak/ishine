package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.FinalDocument;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetails;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsRepository;
import com.apmosys.employeeportal.service.TimesheetDocumentService;
import com.apmosys.employeeportal.service.TimesheetDocumentServiceNew;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/timesheetUploadTest")
public class TimesheetUploadTestController {

    @Autowired
    private TimesheetDocumentDetailsRepository timesheetDocumentDetailsRepository;

    @Value("${upload.path}")
    private String storagePath;

    @Autowired
    private TimesheetDocumentServiceNew timesheetDocumentServiceNew;

    @Autowired
    private FinalDocumentNewRepository finalDocumentNewRepository;

    @Autowired
    private TimesheetDocumentDetailsNewRepository timesheetDocumentDetailsNewRepository;

    @Autowired
    private FinalDocumentRepository finalDocumentRepository;

    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "xls", "xlsx", "jpg", "jpeg", "png");

    /**
     * Bulk migrate all documents from TimesheetDocumentDetails to
     * TimesheetDocumentDetailsNew
     * 
     * @apiNote used to migrate the timesheet document details from old to new
     */
    @Transactional
    @PutMapping("/bulk-migrate")
    public ResponseEntity<?> bulkMigrateAllDocuments() {
        try {
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            List<Map<String, Object>> allMigrationResults = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            List<TimesheetDocumentDetails> allInitialTimesheetDocumentDetails = timesheetDocumentDetailsRepository
                    .findAllFinalFlaggedDocuments(false);

            if (allInitialTimesheetDocumentDetails == null || allInitialTimesheetDocumentDetails.isEmpty()) {
                return ResponseEntity.ok(allMigrationResults);
            }

            List<TimesheetDocumentDetailsNew> timesheetDocumentDetailsNewsToSave = new ArrayList<>();

            for (TimesheetDocumentDetails document : allInitialTimesheetDocumentDetails) {
                if (document.getFinalFlag() == null || !document.getFinalFlag()) {
                    TimesheetDocumentDetailsNew timesheetDocumentDetailsNew = new TimesheetDocumentDetailsNew();
                    timesheetDocumentDetailsNew.setDocId(document.getDocId());
                    timesheetDocumentDetailsNew.setActive(document.getActive());
                    timesheetDocumentDetailsNew.setDocName(document.getDocName());
                    timesheetDocumentDetailsNew.setTimesheetId(document.getTimesheetId());

                    Integer projectId = employeeTimesheetsNewRepository
                            .findProjectIdByTimesheetId(document.getTimesheetId());

                    String uniqueFileName = projectId + "_" + document.getFinalFlag() + "_" + document.getDocName();

                    timesheetDocumentDetailsNew.setProjectId(projectId);
                    timesheetDocumentDetailsNew.setFileUrl(uniqueFileName);
                    timesheetDocumentDetailsNew.setFinalFlag(document.getFinalFlag());
                    timesheetDocumentDetailsNew.setUpdatedBy(document.getUpdatedBy());
                    timesheetDocumentDetailsNew.setUpdatedOn(document.getUpdatedOn());
                    timesheetDocumentDetailsNew.setMimeTypeId(
                            timesheetDocumentServiceNew.getMimeTypeId(document.getDocMimeType(),
                                    document.getDocName()));
                    timesheetDocumentDetailsNew.setClientApprovalStatusId(
                            timesheetDocumentServiceNew.getClientApprovalStatusId(document.getClientApprovalStatus()));
                    timesheetDocumentDetailsNew.setCreatedOn(document.getCreatedOn() == null ? document.getUpdatedOn() : document.getCreatedOn());
                    timesheetDocumentDetailsNew.setCreatedBy(document.getCreatedBy() == null ? document.getEmpId() : document.getCreatedBy());

                    timesheetDocumentDetailsNewsToSave.add(timesheetDocumentDetailsNew);
                    Path filePath = storageDir.resolve(uniqueFileName);
                    Files.write(filePath, document.getDocData());
                }
            }

            timesheetDocumentDetailsNewRepository.saveAll(timesheetDocumentDetailsNewsToSave);

            List<TimesheetDocumentDetails> allFinalTimesheetDocumentDetails = timesheetDocumentDetailsRepository
                    .findAllFinalFlaggedDocuments(true);

            List<FinalDocumentNew> finalDocumentNewsToSave = new ArrayList<>();

            for (TimesheetDocumentDetails document : allFinalTimesheetDocumentDetails) {
                try {
                    Long timesheetId = document.getTimesheetId();

                    List<TimesheetDocumentDetails> tddList = timesheetDocumentDetailsRepository
                            .findAllByTimesheetId(timesheetId);

                    if (tddList == null || tddList.isEmpty()) {
                        throw new Exception("Timesheet Document Details not found for timesheetId: " + timesheetId);
                    }

                    TimesheetDocumentDetails tdd = tddList.get(0);

                    FinalDocumentNew finalDocumentNew = new FinalDocumentNew();
                    finalDocumentNew.setCreatedBy(tdd.getCreatedBy() == null ? tdd.getEmpId() : tdd.getCreatedBy());
                    finalDocumentNew.setCreatedOn(tdd.getCreatedOn() == null ? tdd.getCreatedOn() : tdd.getCreatedOn());
                    finalDocumentNew.setUpdatedBy(tdd.getUpdatedBy());
                    finalDocumentNew.setUpdatedOn(tdd.getUpdatedOn());
                    finalDocumentNew.setDocName(tdd.getDocName());
                    finalDocumentNew.setMimeTypeId(
                            timesheetDocumentServiceNew.getMimeTypeId(tdd.getDocMimeType(), tdd.getDocName()));

                    Integer projectId = employeeTimesheetsNewRepository
                            .findProjectIdByTimesheetId(document.getTimesheetId());

                    String uniqueFileName = projectId + "_" + document.getFinalFlag() + "_" + document.getDocName();

                    finalDocumentNew.setProjectId(projectId);
                    finalDocumentNew.setFileUrl(uniqueFileName);

                    finalDocumentNewsToSave.add(finalDocumentNew);
                    Path filePath = storageDir.resolve(uniqueFileName);
                    Files.write(filePath, document.getDocData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            finalDocumentNewRepository.saveAll(finalDocumentNewsToSave);

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Bulk migration completed successfully",
                    "time Taken", (System.currentTimeMillis() - startTime) / 1000.0));

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Bulk migration failed: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Bulk migrate all documents from FinalDocument to FinalDocumentNew
     * 
     * @apiNote Only for UAT environment
     * @return
     */
    @Transactional
    @PutMapping("/bulk-migrate-uat")
    public ResponseEntity<?> bulkMigrateAllDocumentsUat() {
        try {
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            final int BATCH_SIZE = 50;
            int currentPage = 0;
            boolean hasMoreRecords = true;

            List<Map<String, Object>> allMigrationResults = new ArrayList<>();
            int totalSuccessCount = 0;
            int totalFailCount = 0;
            int totalSkippedCount = 0;

            long startTime = System.currentTimeMillis();

            while (hasMoreRecords) {
                Pageable pageable = PageRequest.of(currentPage, BATCH_SIZE, Sort.by("docId").ascending());
                List<FinalDocument> batch = finalDocumentRepository.findAll(pageable)
                        .getContent();

                if (batch.isEmpty()) {
                    hasMoreRecords = false;
                    break;
                }

                List<Map<String, Object>> batchResults = new ArrayList<>();
                int batchSuccessCount = 0;
                int batchFailCount = 0;
                int batchSkippedCount = 0;

                for (FinalDocument document : batch) {
                    FinalDocumentNew newDocument = new FinalDocumentNew();
                    System.out.println("For doc Id: " + document.getDocId());
                    newDocument.setFinalDocId(document.getDocId());
                    newDocument.setDocName(document.getDocName());
                    newDocument.setMimeTypeId(
                            timesheetDocumentServiceNew.getMimeTypeId(document.getDocMimeType(), document.getDocName()));
                    newDocument.setCreatedOn(document.getCreatedOn());
                    newDocument.setUpdatedOn(document.getUpdatedOn());
                    newDocument.setUpdatedBy(document.getUpdatedBy());
                    newDocument.setCreatedBy(
                            document.getCreatedBy() == null ? 123 : document.getCreatedBy());
                    Map<String, Object> result = new HashMap<>();
                    result.put("docId", document.getDocId());
                    result.put("docName", document.getDocName());
                    result.put("batch", currentPage + 1);

                    if (document.getDocData() != null && document.getDocData().length > 0) {
                        try {
                            String fileExtension = getExtensionFromMimeType(document.getDocMimeType());
                            if (fileExtension.isEmpty() && document.getDocName() != null) {
                                fileExtension = getFileExtension(document.getDocName());
                            }
                            if (fileExtension.isEmpty())
                                fileExtension = "dat";

                            // Since this is for UAT environment, we will save file with uuid
                            String uniqueFileName = "timesheet_doc_final_" + newDocument.getCreatedBy()
                                    + "_" + UUID.randomUUID().toString() + "."
                                    + getFileExtension(document.getDocName());
                            newDocument.setFileUrl(uniqueFileName);

                            // Disussion
                            // FinalDocumentNew newFinalDocument = null;
                            // if (newDocument.getFinalFlag()) {
                            // newFinalDocument = new FinalDocumentNew();
                            // newFinalDocument.setDocName(newDocument.getDocName());
                            // newFinalDocument.setMimeTypeId(newDocument.getMimeTypeId());
                            // newFinalDocument.setCreatedOn(newDocument.getCreatedOn());
                            // newFinalDocument.setUpdatedOn(newDocument.getUpdatedOn());
                            // newFinalDocument.setUpdatedBy(newDocument.getUpdatedBy());
                            // newFinalDocument.setCreatedBy(newDocument.getCreatedBy());
                            // newFinalDocument.setFileUrl(newDocument.getFileUrl());
                            // newFinalDocument = finalDocumentNewRepository.save(newFinalDocument);
                            // }
                            // if(newFinalDocument != null){
                            // newDocument.setBulkApprovedDocId(newFinalDocument.getFinalDocId());
                            // }
                            finalDocumentNewRepository.save(newDocument);

                            if (!uniqueFileName.contains(".")) {
                                uniqueFileName += "." + fileExtension;
                            }

                            Path filePath = storageDir.resolve(uniqueFileName);
                            Files.write(filePath, document.getDocData());

                            result.put("status", "SUCCESS");
                            result.put("filePath", filePath.toString());
                            result.put("fileSize", document.getDocData().length);
                            result.put("fileName", uniqueFileName);
                            batchSuccessCount++;
                            totalSuccessCount++;
                        } catch (IOException e) {
                            result.put("status", "FAILED");
                            result.put("error", e.getMessage());
                            batchFailCount++;
                            totalFailCount++;
                        }
                    } else {
                        result.put("status", "FAILED");
                        result.put("error", "No data in database");
                        batchFailCount++;
                        totalFailCount++;
                    }
                    batchResults.add(result);
                }

                Map<String, Object> batchSummary = new HashMap<>();
                batchSummary.put("batchNumber", currentPage + 1);
                batchSummary.put("startIndex", currentPage * BATCH_SIZE);
                batchSummary.put("endIndex", (currentPage * BATCH_SIZE) + batch.size() - 1);
                batchSummary.put("batchSize", batch.size());
                batchSummary.put("successInBatch", batchSuccessCount);
                batchSummary.put("failedInBatch", batchFailCount);
                batchSummary.put("skippedInBatch", batchSkippedCount);
                batchSummary.put("processedAt", LocalDateTime.now());

                allMigrationResults.add(batchSummary);
                allMigrationResults.addAll(batchResults);

                currentPage++;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTimeSeconds = (endTime - startTime) / 1000;

            long totalDocuments = timesheetDocumentDetailsRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "COMPLETED");
            response.put("message", "Bulk migration completed successfully");
            response.put("totalBatchesProcessed", currentPage);
            response.put("batchSize", BATCH_SIZE);
            response.put("totalDocumentsInDB", totalDocuments);
            response.put("totalMigrated", totalSuccessCount);
            response.put("totalFailed", totalFailCount);
            response.put("totalSkipped", totalSkippedCount);
            response.put("totalTimeSeconds", totalTimeSeconds);
            response.put("storagePath", storagePath);
            response.put("startedAt", new Date(startTime));
            response.put("completedAt", new Date(endTime));

            if (allMigrationResults.size() <= 100) {
                response.put("detailedResults", allMigrationResults);
            } else {
                response.put("detailedResults",
                        "Results truncated. " + allMigrationResults.size() + " records processed.");
                response.put("sampleResults", allMigrationResults.subList(0, Math.min(20, allMigrationResults.size())));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Bulk migration failed: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private boolean isAlreadyMigrated(Long docId, Path storageDir) throws IOException {
        if (!Files.exists(storageDir)) {
            return false;
        }

        try (var stream = Files.list(storageDir)) {
            return stream.anyMatch(path -> {
                String filename = path.getFileName().toString();
                // Check for patterns like "doc_1_", "doc_1_2024", etc.
                return filename.matches("doc_" + docId + "_.*") ||
                        filename.matches(".*_" + docId + "_.*") ||
                        filename.startsWith(docId + "_");
            });
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null)
            return "";

        switch (mimeType.toLowerCase()) {
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "application/vnd.ms-excel":
                return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "xlsx";
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "text/plain":
                return "txt";
            default:
                return "";
        }
    }

    private String determineMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xlsm":
                return "application/vnd.ms-excel.sheet.macroEnabled.12";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
}