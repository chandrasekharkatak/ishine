package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DesignationDTO {

	private Long designationId;
	private String designationName;
	private Long createdBy;
	private Long updatedBy;
	private String updatedOn;
	private String createdOn;
	private Long[] deptIdList;
	private String createdByName;
	private String updatedByName;
	private Long deptId;
	private Long newDesignationId;
	
}
