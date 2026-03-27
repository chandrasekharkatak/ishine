package com.apmosys.employeeportal.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
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

import com.apmosys.employeeportal.dto.TimesheetDocumentMetaDto;
import com.apmosys.employeeportal.model.TempFailedDoc;
//import com.apmosys.employeeportal.model.TimesheetFaildDoc;
import com.apmosys.employeeportal.service.MigrationTransactionService;
import com.apmosys.employeeportal.utility.FileNameGenerator;

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

    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MigrationTransactionService migrationTransactionService;
    
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
// need to be discussed.
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
// need discussion.
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

    @Transactional
    @PutMapping("/bulk-migrate2")
    public ResponseEntity<?> bulkMigrateAllDocumentsNew() {
        try {
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
    
            long startTime = System.currentTimeMillis();
            int BATCH_SIZE = 150; 
            int FETCH_SIZE = 20;
    
            List<TimesheetDocumentMetaDto> allMeta = timesheetDocumentDetailsRepository.findAllMetaOnly();
    
            if (allMeta == null || allMeta.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "No documents to migrate"));
            }
    
           
            Map<Long, List<TimesheetDocumentMetaDto>> grouped = allMeta.stream()
                    .collect(Collectors.groupingBy(TimesheetDocumentMetaDto::getTimesheetId));
    
            List<TimesheetDocumentMetaDto> singleEntryDocs = new ArrayList<>();
            Map<Long, TimesheetDocumentMetaDto> multiFinalDocs = new LinkedHashMap<>();
            Map<Long, TimesheetDocumentMetaDto> multiPendingDocs = new LinkedHashMap<>();
    
            for (Map.Entry<Long, List<TimesheetDocumentMetaDto>> entry : grouped.entrySet()) {
                List<TimesheetDocumentMetaDto> docs = entry.getValue();
                if (docs.size() == 1) {
                    singleEntryDocs.add(docs.get(0));
                } else {
                    for (TimesheetDocumentMetaDto doc : docs) {
                        if (Boolean.TRUE.equals(doc.getFinalFlag())) {
                            multiFinalDocs.put(entry.getKey(), doc);
                        } else {
                            multiPendingDocs.put(entry.getKey(), doc);
                        }
                    }
                }
            }
    
            int totalProcessed = 0;
            int totalFailed = 0;
            List<Map<String, Object>> failedRecords = new ArrayList<>();
            List<TempFailedDoc> failedDocsBatch = new ArrayList<>();
    
            List<TimesheetDocumentDetailsNew> batchToSave = new ArrayList<>();
            List<TimesheetDocumentDetailsNew> singleGlobalBatch = new ArrayList<>();
            Map<Long, String> singleDocIdToFileName = new HashMap<>();
            
    

            // this is for sigle entry docs

            for (int i = 0; i < singleEntryDocs.size(); i+= FETCH_SIZE) {
                int end = Math.min(i + FETCH_SIZE, singleEntryDocs.size());

                List<TimesheetDocumentMetaDto> chunk = singleEntryDocs.subList(i, end);

                List<Long> docIds = chunk.stream()
                    .map(TimesheetDocumentMetaDto::getDocId).collect(Collectors.toList());
                List<Long> timesheetIds = chunk.stream()
                    .map(TimesheetDocumentMetaDto::getTimesheetId).collect(Collectors.toList());

                    Map<Long, byte[]> docDataMap;
                    Map<Long, Integer> projectIdMap;

                // TimesheetDocumentMetaDto doc = singleEntryDocs.get(i);
                try {
                  
                    docDataMap = timesheetDocumentDetailsRepository
                    .findDocDataByDocIds1(docIds).stream()
                    .collect(Collectors.toMap(row -> (Long) row[0], row -> (byte[]) row[1]));

                    projectIdMap =
                    	    timesheetDocumentDetailsRepository
                    	        .findProjectIdsByTimesheetIds(timesheetIds)
                    	        .stream()
                    	        .collect(Collectors.toMap(
                    	            row -> ((Number) row[0]).longValue(),
                    	            row -> ((Number) row[1]).intValue()
                    	        ));

                } catch (Exception e) {
                    for (TimesheetDocumentMetaDto doc : chunk) {
                        totalFailed++;
                        failedRecords.add(Map.of(
                                "docId", doc.getDocId(),
                                "timesheetId", doc.getTimesheetId(),
                                "docName", doc.getDocName(),
                                "type", "SINGLE_ENTRY",
                                "reason", "Batch fetch failed: " + e.getMessage()
                        ));
                         failedDocsBatch.add(buildFailedDoc(
                            doc.getDocId(), doc.getDocName(),
                            doc.getTimesheetId(), "SINGLE_ENTRY", e.getMessage()));
                    }
                    continue;
                    
                }
                for (TimesheetDocumentMetaDto doc : chunk) {
                    String uniqueFileName = null;

                    try {
                        Integer projectId = projectIdMap.get(doc.getTimesheetId());
                        byte[] docData = docDataMap.get(doc.getDocId());
    
                        if (projectId == null) throw new RuntimeException("projectId not found for timesheetId: " + doc.getTimesheetId());
                        if (docData == null) throw new RuntimeException("docData not found for docId: " + doc.getDocId());
    
                        // This runs in its own transaction — if it throws, DB rolls back automatically
                        // fileWritten = projectId + "_" + flagToInt(doc.getFinalFlag()) + "_" + doc.getDocName();
                        // uniqueFileName = projectId + "_" + flagToInt(doc.getFinalFlag()) + "_" + doc.getDocName();
                        String ext = doc.getDocName().substring(doc.getDocName().lastIndexOf('.'));
                        uniqueFileName = FileNameGenerator.generate(projectId, ext, "filled");
                        Path filePath = storageDir.resolve(uniqueFileName);
                        Files.write(filePath, docData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                        // migrationTransactionService.migrateSingleEntry(doc, projectId, docData, storageDir);
                        singleGlobalBatch.add(migrationTransactionService.buildTimesheetDocumentDetailsNewFromDto(
                            doc, projectId, uniqueFileName, null, false));
                        singleDocIdToFileName.put(doc.getDocId(), uniqueFileName);
                        
    
                    } catch (Exception e) {
                        totalFailed++;
                        // Clean up file from disk if it was written before DB failed
                        cleanupFile(storageDir, uniqueFileName);
                        failedRecords.add(Map.of(
                                "docId", doc.getDocId(),
                                "timesheetId", doc.getTimesheetId(),
                                "docName", doc.getDocName(),
                                "type", "SINGLE_ENTRY",
                                "reason", e.getMessage()
                        ));

                        failedDocsBatch.add(buildFailedDoc(
                            doc.getDocId(), doc.getDocName(),
                            doc.getTimesheetId(), "SINGLE_ENTRY", e.getMessage() ));
                    }

                }

                boolean isLastChunk = (end == singleEntryDocs.size());
                if (singleGlobalBatch.size() >= BATCH_SIZE || isLastChunk) {
                    if (!singleGlobalBatch.isEmpty()) {
                        try {
                            migrationTransactionService.saveTimesheetDocBatch(singleGlobalBatch);
                            totalProcessed += singleGlobalBatch.size();
                        } catch (Exception e) {
                            // DB batch save failed — clean up all files written in this batch
                            totalFailed += singleGlobalBatch.size();
                            for (TimesheetDocumentDetailsNew failed : singleGlobalBatch) {
                                cleanupFile(storageDir, singleDocIdToFileName.get(failed.getDocId()));
                                failedRecords.add(Map.of(
                                        "docId", failed.getDocId(),
                                        "timesheetId", failed.getTimesheetId(),
                                        "docName", failed.getDocName(),
                                        "type", "SINGLE_ENTRY",
                                        "reason", "DB batch save failed: " + e.getMessage()
                                ));

                                failedDocsBatch.add(buildFailedDoc(
                                    failed.getDocId(), failed.getDocName(),
                                    failed.getTimesheetId(), "SINGLE_ENTRY", e.getMessage()));

                            }
                        } finally {
                            singleGlobalBatch.clear();
                            singleDocIdToFileName.clear();
                        }
                    }
                }



            }
    
            List<Long> timesheetIdList = new ArrayList<>(multiFinalDocs.keySet());

            // this is for final doc
            List<TimesheetDocumentDetailsNew> pendingGlobalBatch = new ArrayList<>();
            
            Map<Long, String[]> pendingTimesheetIdToFileNames = new HashMap<>();

            for (int i = 0; i < timesheetIdList.size(); i += FETCH_SIZE) {
                int end = Math.min(i + FETCH_SIZE, timesheetIdList.size());
                List<Long> chunkTimesheetIds = timesheetIdList.subList(i, end);
    
                List<Long> finalDocIds = chunkTimesheetIds.stream()
                        .map(tid -> multiFinalDocs.get(tid).getDocId()).collect(Collectors.toList());
    
                List<Long> pendingDocIds = chunkTimesheetIds.stream()
                        .filter(tid -> multiPendingDocs.get(tid) != null)
                        .map(tid -> multiPendingDocs.get(tid).getDocId()).collect(Collectors.toList());
    
                List<Long> allDocIds = new ArrayList<>();
                allDocIds.addAll(finalDocIds);
                allDocIds.addAll(pendingDocIds);
    
                Map<Long, byte[]> docDataMap;
                Map<Long, Integer> projectIdMap;
    
                try {
                    docDataMap = timesheetDocumentDetailsRepository
                            .findDocDataByDocIds1(allDocIds).stream()
                            .collect(Collectors.toMap(row -> (Long) row[0], row -> (byte[]) row[1]));
                    
                    projectIdMap =
                    	    timesheetDocumentDetailsRepository
                    	        .findProjectIdsByTimesheetIds(chunkTimesheetIds)
                    	        .stream()
                    	        .collect(Collectors.toMap(
                    	            row -> ((Number) row[0]).longValue(),
                    	            row -> ((Number) row[1]).intValue()
                    	        ));
    
                } catch (Exception e) {
                    for (Long timesheetId : chunkTimesheetIds) {
                        totalFailed++;
                        
                        failedRecords.add(Map.of(
                                "timesheetId", timesheetId,
                                "type", "MULTI_ENTRY",
                                "reason", "Batch fetch failed: " + e.getMessage()
                        ));
                        Long failedFinalDocId = multiFinalDocs.get(timesheetId).getDocId();
                        Long failedPendingDoc = multiPendingDocs.get(timesheetId).getDocId();
                        if(failedFinalDocId != null) {
                        failedDocsBatch.add(buildFailedDoc(
                        		failedFinalDocId, "Not saving here",
                            timesheetId, "MULTI_ENTRY", e.getMessage()));
                        }
                        if(failedPendingDoc != null) {
                            failedDocsBatch.add(buildFailedDoc(
                            		failedPendingDoc, "Not saving here",
                                timesheetId, "MULTI_ENTRY", e.getMessage()));
                            }
                        

                    }
                    continue;
                }
         
                for (Long timesheetId : chunkTimesheetIds) {
                    TimesheetDocumentMetaDto finalDoc = multiFinalDocs.get(timesheetId);
                    TimesheetDocumentMetaDto pendingDoc = multiPendingDocs.get(timesheetId);
                    String finalFileName = null;
                    String pendingFileName = null;
    
                    try {
                        Integer projectId = projectIdMap.get(timesheetId);
                        byte[] finalDocData = docDataMap.get(finalDoc.getDocId());
                        byte[] pendingDocData = pendingDoc != null ? docDataMap.get(pendingDoc.getDocId()) : null;
    
                        if (projectId == null) throw new RuntimeException("projectId not found for timesheetId: " + timesheetId);
                        if (finalDocData == null) throw new RuntimeException("docData not found for final docId: " + finalDoc.getDocId());
    
                        // Both final + pending saved in ONE transaction
                        // If pending DB save fails → final DB save also rolls back automatically


                        // finalFileName = projectId + "_" + flagToInt(finalDoc.getFinalFlag()) + "_" + finalDoc.getDocName();
                        // pendingFileName = pendingDoc != null
                        //     ? projectId + "_" + flagToInt(pendingDoc.getFinalFlag()) + "_" + pendingDoc.getDocName()
                        //     : null;

                        String extPending = pendingDoc.getDocName().substring(pendingDoc.getDocName().lastIndexOf('.'));
                        pendingFileName = FileNameGenerator.generate(projectId, extPending, "filled");
                        // finalFileName = FileUtils.generate(storageDir, finalDoc.getDocName());
                        String extFinal = pendingDoc.getDocName().substring(pendingDoc.getDocName().lastIndexOf('.'));
                        finalFileName = FileNameGenerator.generate(projectId, extFinal, "approved");
                      
                            Files.write(
                                storageDir.resolve(finalFileName),
                                finalDocData,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING
                        );

                        FinalDocumentNew savedFinalDoc = migrationTransactionService.saveFinalDoc(
                            migrationTransactionService.buildFinalDocumentNewFromDto(
                                    finalDoc, projectId, finalFileName));


                        // migrationTransactionService.migrateMultiEntry(
                        //         finalDoc, pendingDoc, projectId, finalDocData, pendingDocData, storageDir);

                        if (pendingDoc != null && pendingDocData != null && pendingFileName != null) {
                            Files.write(
                                    storageDir.resolve(pendingFileName),
                                    pendingDocData,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING
                            );
                            pendingGlobalBatch.add(
                                    migrationTransactionService.buildTimesheetDocumentDetailsNewFromDto(
                                            pendingDoc, projectId, pendingFileName,
                                            savedFinalDoc.getFinalDocId(), true));
    
                            pendingTimesheetIdToFileNames.put(timesheetId,
                                    new String[]{finalFileName, pendingFileName});
                        }

                        totalProcessed++;
    
                    } catch (Exception e) {
                        totalFailed++;
                        // Clean up both files from disk if written before failure
                        cleanupFile(storageDir, finalFileName);
                        cleanupFile(storageDir, pendingFileName);
                        String reason = e.getMessage();
                        failedRecords.add(Map.of(
                                "docId", finalDoc.getDocId(),
                                "timesheetId", timesheetId,
                                "docName", finalDoc.getDocName(),
                                "type", "MULTI_ENTRY",
                                "reason", e.getMessage()
                        ));

                        failedDocsBatch.add(buildFailedDoc(
                            finalDoc.getDocId(), finalDoc.getDocName(),
                            timesheetId, "MULTI_ENTRY", reason));
                    }
                }
                boolean isLastChunk = (end == timesheetIdList.size());
                if (pendingGlobalBatch.size() >= BATCH_SIZE || isLastChunk) {
                    if (!pendingGlobalBatch.isEmpty()) {
                        try {
                            migrationTransactionService.saveTimesheetDocBatch(pendingGlobalBatch);
                        } catch (Exception e) {
                            
                            for (TimesheetDocumentDetailsNew failed : pendingGlobalBatch) {
                                Long tid = failed.getTimesheetId();
                                String[] fileNames = pendingTimesheetIdToFileNames.get(tid);
                                if (fileNames != null) {
                                    
                                    cleanupFile(storageDir, fileNames[1]);
                                }
                                failedRecords.add(Map.of(
                                        "docId", failed.getDocId(),
                                        "timesheetId", failed.getTimesheetId(),
                                        "docName", failed.getDocName(),
                                        "type", "MULTI_ENTRY_PENDING",
                                        "reason", "DB batch save failed: " + e.getMessage()
                                ));

                                failedDocsBatch.add(buildFailedDoc(
                                    failed.getDocId(), failed.getDocName(),
                                    failed.getTimesheetId(), "MULTI_ENTRY_PENDING", e.getMessage()));

                            }
                        } finally {
                            pendingGlobalBatch.clear();
                            pendingTimesheetIdToFileNames.clear();
                        }
                    }
                }
    
        }

        if (!failedDocsBatch.isEmpty()) {
            try {
                migrationTransactionService.saveFailedRecords(failedDocsBatch);
            } catch (Exception e) {
                // Log but don't fail the whole response — migration itself is done
                System.err.println("Warning: Could not save failed records to temp table: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Bulk migration completed",
            "totalProcessed", totalProcessed,
            "totalFailed", totalFailed,
            "timeTaken", (System.currentTimeMillis() - startTime) / 1000.0 + "s",
            "failedRecords", failedRecords
    ));

        

    }
    catch(Exception e){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Bulk migration failed: " + e.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }


}

private void cleanupFile(Path storageDir, String fileName) {
    if (fileName == null) return;
    try {
        Files.deleteIfExists(storageDir.resolve(fileName));
    } catch (Exception ignored) {}
}

private int flagToInt(Boolean flag) {
    return Boolean.TRUE.equals(flag) ? 1 : 0;
}


private TempFailedDoc buildFailedDoc(Long docId, String docName, Long timesheetId,
                                      String type, String reason) {
    return new TempFailedDoc(docId, docName, timesheetId, type, reason);

}

}