package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimesheetApprovalAllocationLogsDTO {
	
	    private Long allocLogId;
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
	    private Integer rejectionLevel;
	    private Integer hierarchyOrder;
}
