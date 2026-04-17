package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HelpDTO {

private Long helpDocId;
	
	private String helpDocumentName;
	
	private String fileName;
	
	private byte[] fileBytes;
	
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String updatedByName;
	private String createdOn;
	private String updatedOn;
	private Long empId;
	private String name;
	
}
