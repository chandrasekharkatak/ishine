package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdOn;
	private Long createdBy;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private Boolean active;
	private String clientApprovalStatus;
	private String rmApprovalStatus;
	private String hrApprovalStatus;
	private Boolean finalFlag;
	private byte[] docData;
	 
    public TimesheetDocumentDetailsDTO(Long docId, String docName, Long timesheetId, Long empId,
                                       Boolean active, String clientApprovalStatus,
                                       String rmApprovalStatus, String hrApprovalStatus, Boolean finalFlag) {
        this.docId = docId;
        this.docName = docName;
        this.timesheetId = timesheetId;
        this.empId = empId;
        this.active = active;
        this.clientApprovalStatus = clientApprovalStatus;
        this.rmApprovalStatus = rmApprovalStatus;
        this.hrApprovalStatus = hrApprovalStatus;
        this.finalFlag = finalFlag;
    }
}
