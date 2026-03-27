package com.apmosys.employeeportal.dto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentMetaDto {
    private Long docId;
    private Long timesheetId;
    private String docName;
    private String docMimeType;
    private Boolean finalFlag;
    private Boolean active;
    private String clientApprovalStatus;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
    private Long empId;
    
    
    public TimesheetDocumentMetaDto(
            Long docId,
            Long timesheetId,
            String docName,
            String docMimeType,
            Boolean finalFlag,
            Boolean active,
            String clientApprovalStatus,
//            Long createdBy,
            LocalDateTime createdOn,
//            Long updatedBy,
            LocalDateTime updatedOn
//            Long empId
    ) {
        this.docId = docId;
        this.timesheetId = timesheetId;
        this.docName = docName;
        this.docMimeType = docMimeType;
        this.finalFlag = finalFlag;
        this.active = active;
        this.clientApprovalStatus = clientApprovalStatus;
//        this.createdBy = createdBy;
        this.createdOn = createdOn;
//        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
//        this.empId = empId;
    }
    
}