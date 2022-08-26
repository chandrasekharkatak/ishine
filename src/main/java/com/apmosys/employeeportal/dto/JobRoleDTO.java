package com.apmosys.employeeportal.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class JobRoleDTO {

	private Long jobRoleId;	
	private String name;	
	private Long departmentId;	
	private String createdOn;	
	private String createdBy;	
//	private LocalDateTime updatedOn;	
	private int updatedBy;
	private int createdById;
	private String departmentName;
	private Long hodId;
	private String updatedByName;
	private String updatedOn;
	private Long oldJobRoleId;
	private String employeeRole;
	
	
	
	
	
	
	
	
}
