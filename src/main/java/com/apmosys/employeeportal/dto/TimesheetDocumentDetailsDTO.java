package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentDetailsDTO {
	private Long docId;
	private String docName;
	private byte[] docData;
	private Long timesheetId;
	private Long empId;
	private LocalDateTime createdOn;
	private Long createdBy;
	private LocalDateTime updatedOn;
	private Long updated_by;
	private Boolean active;
	private String clientApprovalStatus;
	private String rmApprovalStatus;
	private Boolean finalFlag;
}
