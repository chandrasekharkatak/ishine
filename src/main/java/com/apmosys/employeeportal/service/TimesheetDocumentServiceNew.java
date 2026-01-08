package com.apmosys.employeeportal.service;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.apmosys.employeeportal.dto.ProjectTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;
import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;
import com.apmosys.employeeportal.repository.ClientStatusMasterNewRepository;
import com.apmosys.employeeportal.repository.DocMimeTypeMasterNewRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;

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
            EmployeeTimesheetDTO newEmpDTO,
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

        // Create file lookup map - docName must match original filename
        Map<String, MultipartFile> fileMap = documents.stream()
                .collect(Collectors.toMap(
                        MultipartFile::getOriginalFilename,
                        f -> f,
                        (a, b) -> a));

        // Load existing docs by docId for updates
        // List<Long> docIds = documentDataList.stream()
        // .map(TimesheetDocumentDataDTO::getDocId)
        // .filter(Objects::nonNull)
        // .distinct()
        // .collect(Collectors.toList());

        // Map<Long, TimesheetDocumentDetailsNew> existingById = !docIds.isEmpty()
        // ? timesheetDocumentDetailsNewRepository.findAllById(docIds)
        // .stream()
        // .collect(Collectors.toMap(
        // TimesheetDocumentDetailsNew::getDocId,
        // d -> d))
        // : Collections.emptyMap();

        // Map to track Filled documents processed in current batch, keyed by projectId
        // Map<Long, TimesheetDocumentDetailsNew> filledDocumentsInBatch = new
        // HashMap<>();

        // for (TimesheetDocumentDataDTO docData : documentDataList) {
        // // Verify file exists for this document
        // MultipartFile matchedFile = fileMap.get(docData.getDocName());
        // if (matchedFile == null) {
        // throw new IllegalArgumentException(
        // "Document is missing: " + docData.getDocName());
        // }

        // if ("Filled".equalsIgnoreCase(docData.getDocType())) {
        // TimesheetDocumentDetailsNew filledDoc;

        // // Check if updating existing or creating new
        // if (docData.getDocId() != null &&
        // existingById.containsKey(docData.getDocId())) {
        // // Update existing Filled document from database
        // filledDoc = existingById.get(docData.getDocId());
        // } else {
        // // Create new Filled document
        // filledDoc = new TimesheetDocumentDetailsNew();
        // filledDoc.setDocId(null);
        // }

        // // Set Filled document data
        // filledDoc.setTimesheetId(timesheetId);
        // filledDoc.setFilledDocName(docData.getDocName());
        // filledDoc.setFilledFileUrl(docData.getUniqueIdentifier());
        // filledDoc.setFilledMimeTypeId(
        // getMimeTypeId(
        // matchedFile.getContentType(),
        // docData.getUniqueIdentifier()));
        // filledDoc.setFinalFlag(false);
        // filledDoc.setClientApprovalStatusId(1);
        // filledDoc.setActive(true);

        // toSave.add(filledDoc);

        // // Store in map using projectId for Approved documents to find
        // // Only if projectId is provided (for new documents in batch)
        // if (docData.getProjectId() != null) {
        // filledDocumentsInBatch.put(docData.getProjectId(), filledDoc);
        // } else {
        // throw new IllegalArgumentException("ProjectId is required for documents");
        // }

        // } else if ("Approved".equalsIgnoreCase(docData.getDocType())) {
        // TimesheetDocumentDetailsNew filledDocToUpdate = null;

        // // Priority 1: Try to find by docId (existing document in database)
        // if (docData.getDocId() != null) {
        // filledDocToUpdate = existingById.get(docData.getDocId());
        // }

        // // Priority 2: Try to find by projectId in current batch
        // if (filledDocToUpdate == null && docData.getProjectId() != null) {
        // filledDocToUpdate = filledDocumentsInBatch.get(docData.getProjectId());
        // }

        // // If still not found, throw error
        // if (filledDocToUpdate == null) {
        // throw new IllegalStateException(
        // "Cannot find corresponding Filled document for Approved: " +
        // docData.getDocName()
        // + ". Provide either valid docId for existing document or projectId for new
        // document in same batch.");
        // }

        // // Ensure this document will be saved
        // if (!toSave.contains(filledDocToUpdate)) {
        // toSave.add(filledDocToUpdate);
        // }

        // // Update with Approved document information
        // filledDocToUpdate.setApprovedDocName(docData.getDocName());
        // filledDocToUpdate.setApprovedFileUrl(docData.getUniqueIdentifier());
        // filledDocToUpdate.setApprovedMimeTypeId(
        // getMimeTypeId(
        // matchedFile.getContentType(),
        // docData.getUniqueIdentifier()));
        // filledDocToUpdate.setFinalFlag(true);
        // filledDocToUpdate.setClientApprovalStatusId(2);
        // }
        // }

        for (TimesheetDocumentDataDTO docData : documentDataList) {
            try {
                // Verify file exists for this document
                MultipartFile matchedFile = fileMap.get(docData.getUniqueIdentifier());
                if (matchedFile == null) {
                    throw new IllegalArgumentException(
                            "Document is missing: " + docData.getDocName());
                }

                if ("Filled".equalsIgnoreCase(docData.getDocType())) {
                    TimesheetDocumentDetailsNew filledDoc = new TimesheetDocumentDetailsNew();

                    if (docData.getDocId() != null) {
                        TimesheetDocumentDetailsNew existingDoc = timesheetDocumentDetailsNewRepository
                                .findById(docData.getDocId()).orElse(null);
                        if (existingDoc != null) {
                            deleteFile(existingDoc.getFileUrl());
                        }
                        filledDoc.setDocId(docData.getDocId());
                    }
                    filledDoc.setFileUrl(docData.getUniqueIdentifier());
                    filledDoc.setDocName(docData.getDocName());
                    filledDoc.setMimeTypeId(getMimeTypeId(matchedFile.getContentType(), docData.getUniqueIdentifier()));
                    filledDoc.setClientApprovalStatusId(1);
                    filledDoc.setActive(true);
                    filledDoc.setTimesheetId(timesheetId);
                    timesheetDocumentDetailsNewRepository.save(filledDoc);

                } else if ("Approved".equalsIgnoreCase(docData.getDocType())) {
                    // timsheetID for safety
                    List<TimesheetDocumentDetailsNew> existingDetailsNew = timesheetDocumentDetailsNewRepository
                            .findByTimesheetIdAndActive(timesheetId);
                    if (existingDetailsNew == null || existingDetailsNew.isEmpty()) {
                        throw new IllegalArgumentException("Document is missing: " + docData.getDocName());
                    }
                    FinalDocumentNew finalDocumentNew = new FinalDocumentNew();

                    if (docData.getBulkApprovedDocId() != null) {
                        FinalDocumentNew existingDoc = finalDocumentNewRepository
                                .findById(docData.getBulkApprovedDocId()).orElse(null);
                        if (existingDoc != null) {
                            deleteFile(existingDoc.getFileUrl());
                        }
                        finalDocumentNew.setFinalDocId(docData.getBulkApprovedDocId());
                    }

                    finalDocumentNew.setFileUrl(docData.getUniqueIdentifier());
                    finalDocumentNew.setCreatedBy(newEmpDTO.getCreatedBy());
                    finalDocumentNew.setUpdatedBy(newEmpDTO.getUpdatedBy());
                    finalDocumentNew.setDocName(docData.getDocName());
                    finalDocumentNew.setMimeTypeId(getMimeTypeId(docData.getDocName(), docData.getUniqueIdentifier()));
                    finalDocumentNew = finalDocumentNewRepository.save(finalDocumentNew);

                    for (TimesheetDocumentDetailsNew doc : existingDetailsNew) {
                        doc.setBulkApprovedDocId(finalDocumentNew.getFinalDocId());
                        doc.setFinalFlag(true);
                        doc.setClientApprovalStatusId(2);
                        timesheetDocumentDetailsNewRepository.save(doc);
                    }

                }
                uploadFile(documents);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                uploadFile(file, file.getName());
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
