package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeDetailsForTeamMemberDTO {
	
	private Long empId;
	private Long employeementId;
	private String name;
	private Long jobRoleId;
	private String jobRoleName;
	private String departmentId;
//	private String employeeRole;
	private String isTeamLead;
	private String startDate;
	private String employeeName;
	private String deptName;
	private Long deptId;
	private String isConsultant;
}
