package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {

	private Integer projectId;
	private String clientName;
	private String clientLocation;
	private String state;
	private String projectName;
	private String description;
	private Long projectManagerId;
	private Long empId;
	private Timestamp approvedOn;
	private Timestamp createdOn;
	private Long employeementId;
	private String departmentName;

}
