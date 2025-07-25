package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDocumentDetailsDTO {
	private Long docId;
	private String docName;
	private MultipartFile docFile;
	private String docDataBase64;
	private String mimeType;
	private Long timesheetId;
	private Long empId;
	private LocalDateTime createdOn;
	private Long createdBy;
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private Boolean active;
	private String clientApprovalStatus;
	private String rmApprovalStatus;
	private Boolean finalFlag;
}
