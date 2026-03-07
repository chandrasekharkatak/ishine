package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import com.apmosys.employeeportal.model.FinalDocumentNew;
import com.apmosys.employeeportal.model.TimesheetDocumentDetailsNew;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing document data for timesheet.
 * NEW CONTRACT: Document data is at employee level, linked to projects.
 * Storage: Filled docs in TimesheetDocumentDetailsNew; Approved docs in FinalDocumentNew
 * (linked via bulkApprovedDocId on the filled row).
 *
 * @author System
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentDataDTO {

    /**
     * Document ID (null for new documents)
     * For Filled: TimesheetDocumentDetailsNew.docId; For Approved: FinalDocumentNew.finalDocId
     */
    private Long docId;

    /**
     * Project ID this document is associated with
     * Required for creation
     */
    private Integer projectId;

    /**
     * Document name (e.g., "Screenshot_Approved", "Screenshot_Filled")
     * Required for creation
     */
    private String docName;

    /**
     * Final flag - true for approved documents, false for filled documents
     * Required for creation
     */
    private Boolean finalFlag;

    /**
     * Bulk approved document ID (for bulk approval scenarios)
     * Set for Filled rows when Approved doc exists; points to FinalDocumentNew.finalDocId
     */
    private Long bulkApprovedDocId;

    /**
     * Unique identifier for document tracking (e.g. file path/url)
     * Optional
     */
    private String uniqueIdentifier;

    /**
     * Document Type -> "Filled", "Approved"
     * Required for creation
     */
    private String docType;

    private Long timesheetId;

    /**
     * Creates DTO for Filled document from TimesheetDocumentDetailsNew.
     * TimesheetDocumentDetailsNew always represents the FILLED doc; Approved metadata is in FinalDocumentNew.
     */
    public TimesheetDocumentDataDTO(TimesheetDocumentDetailsNew doc) {
        this.docId = doc.getDocId();
        this.projectId = doc.getProjectId();
        this.docName = doc.getDocName();
        this.finalFlag = doc.getFinalFlag();
        this.bulkApprovedDocId = doc.getBulkApprovedDocId();
        this.uniqueIdentifier = doc.getFileUrl() != null ? doc.getFileUrl() : doc.getDocName();
        this.docType = "Filled";
    }

    /**
     * Creates DTO for Approved document from FinalDocumentNew.
     */
    public static TimesheetDocumentDataDTO fromFinalDocument(FinalDocumentNew finalDoc) {
        TimesheetDocumentDataDTO dto = new TimesheetDocumentDataDTO();
        dto.setDocId(finalDoc.getFinalDocId());
        dto.setProjectId(finalDoc.getProjectId());
        dto.setDocName(finalDoc.getDocName());
        dto.setFinalFlag(true);
        dto.setBulkApprovedDocId(null);
        dto.setUniqueIdentifier(finalDoc.getFileUrl() != null ? finalDoc.getFileUrl() : finalDoc.getDocName());
        dto.setDocType("Approved");
        return dto;
    }
}

