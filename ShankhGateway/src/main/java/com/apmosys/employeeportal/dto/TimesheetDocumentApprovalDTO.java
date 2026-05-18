package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimesheetDocumentApprovalDTO {
	
	    private Long allocId;
	    private Long timesheetId;
	    private Long approverId;
	    private String approvalStatus;
	    private Integer levelId;
	    private Integer previousLevelId;
	    private Long previousApproverId;
	    private Integer rejectionId;
	    private Long createdBy;
	    private LocalDateTime createdOn;
	    private Long updatedBy;
	    private LocalDateTime updatedOn;
	    private Integer rejectionLevel;
	    private Integer hierarchyOrder;
	    private String reason;
	    private Long count;

	    public TimesheetDocumentApprovalDTO(String reason, Long count) {
	        this.reason = reason;
	        this.count = count;
	    }
}
