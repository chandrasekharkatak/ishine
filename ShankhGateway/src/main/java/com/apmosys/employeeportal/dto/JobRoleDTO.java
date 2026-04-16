package com.apmosys.employeeportal.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobRoleDTO {

	private Long jobRoleId;	
	private String name;	
	private Long departmentId;	
	private String createdOn;	
	private Long createdBy;	
	private String createdByName;
//	private LocalDateTime updatedOn;	
	private int updatedBy;
	private int createdById;
	private String departmentName;
	private Long hodId;
	private String updatedByName;
	private String updatedOn;
	private Long oldJobRoleId;
	private String employeeRole;
	private List<EmployeeDTO> updatedJobRoleFeatureMapping;
	private List<FeatureMasterDTO> updateDefaultFeatureMapping;
	
	private Long roleId;
	private String roleName;
	private Long deptId;
	private String deptName;
	
	private String isJobRoleUsedInIshine;
	private String isJobRoleUsedInPoPortal;
	
	public JobRoleDTO(String departmentName,String name,String employeeRole) {
		this.departmentName = departmentName;
		this.name = name;
		this.employeeRole = employeeRole;
	}
	
	
}
