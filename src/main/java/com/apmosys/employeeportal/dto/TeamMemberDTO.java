package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TeamMemberDTO {

	private Long empId;
	private String name;
	private String jobRoleName;
	private String departmentId;
	private String[] employeeRole;
	private String isTeamLead;
	private String startDate;
	
	// added for resource
	
	private String employeeName;
	private Long employeementId;
	private String departmentName;
	
}
