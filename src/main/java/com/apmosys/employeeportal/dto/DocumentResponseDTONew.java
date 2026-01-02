package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTONew {
    
    private Long docId;
    private Long timesheetId;
    private String fileUrl;
    private String docName;
    private Integer mimeTypeId;
    private String mimeType;
    private Integer clientApprovalStatusId;
    private String clientApprovalStatus;
    private Boolean active;
    private Boolean finalFlag;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
}

