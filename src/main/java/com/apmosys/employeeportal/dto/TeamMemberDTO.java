package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

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
	private String employeeName;
	private Long employeementId;
	private String employmentIdEmployeeType;
	private String departmentName;
	private Long id;
	private Integer isShadow;
	private Integer isDefaultProject;
    private Long resourceOverviewId;
	private SpocDTO shadow;
	private List<Map<String, Object>> otherActiveProjects;
	
}
