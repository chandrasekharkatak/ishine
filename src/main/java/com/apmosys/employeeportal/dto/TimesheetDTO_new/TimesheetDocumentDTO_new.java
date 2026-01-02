package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentDTO_new {
   


    private Long docId;

    private Long timesheetId; // important for edit / fetch

    private String fileUrl;
    private String docName;

    private Integer mimeTypeId;

    private Integer clientApprovalStatusId;

    private Boolean active;
    private Boolean finalFlag;

    private Long bulkApprovedDocId;

    private Long createdBy;
    private LocalDateTime createdOn;

    private Long updatedBy;
    private LocalDateTime updatedOn;
}


