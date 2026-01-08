package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentDetailsNewDTO {
    private Long docId;
    private Long empId;
    private Long timesheetId;
    private String filledFileUrl;
    private String approvedFileUrl;
    private String filledDocName;
    private String approvedDocName;
    private Integer filledMimeTypeId;
    private Integer approvedMimeTypeId;
    private Integer clientApprovalStatusId;
    private String clientApprovalStatus;
    private Long createdBy;
    private LocalDateTime createdOn;
    private Long updatedBy;
    private LocalDateTime updatedOn;
    private Boolean active;
    private Boolean finalFlag;

    public TimesheetDocumentDetailsNewDTO(
            Long docId, String filledDocName, String approvedDocName, Long timesheetId, Boolean active,
            Integer clientApprovalStatusId, Boolean finalFlag) {
        this.docId = docId;
        this.filledDocName = filledDocName;
        this.approvedDocName = approvedDocName;
        this.timesheetId = timesheetId;
        this.clientApprovalStatusId = clientApprovalStatusId;
        this.active = active;
        this.finalFlag = finalFlag;
    }

    public TimesheetDocumentDetailsNewDTO(
            Long docId, String filledDocName, String approvedDocName, Long timesheetId, Long empId, Boolean active,
            Integer clientApprovalStatusId, Boolean finalFlag) {
        this.docId = docId;
        this.filledDocName = filledDocName;
        this.approvedDocName = approvedDocName;
        this.timesheetId = timesheetId;
        this.empId = empId;
        this.active = active;
        this.clientApprovalStatusId = clientApprovalStatusId;
        this.finalFlag = finalFlag;
    }
}
