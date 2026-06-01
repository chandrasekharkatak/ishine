package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeExitDTO {

	private Long employeeResignationId;
	private String resignationStatus;
	private Long empId;
	private String createdOn;
	private Long statusUpdatedBy;
	private String rejectReason;
	private String resignationMail;
	private String dateOfResign;
	private String dateOfRelieving;
	private Long employmentId;
	private String name;
	private String deptName;
	private String statusUpdatedByName;
	private String statusUpdatedOn;
	private String revokeReason;
	
	private String isConsultant;
	private String isApmosysProduct;
    private String employmentIdAcToET;
    private Long employeementId;
	
	
}
