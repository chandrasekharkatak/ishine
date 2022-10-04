package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeDocumentDTO {

	private Long employeeDocumentId;
	private String documentName;
	private byte[] documentBytes;
	
	private String documentType;
	private String uploadStatus;
	private String isDraft;
	
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	
	
}
