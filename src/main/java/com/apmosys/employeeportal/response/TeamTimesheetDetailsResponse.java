package com.apmosys.employeeportal.response;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamTimesheetDetailsResponse {

	private Long empId;
	private Long employementId;
	private String empName;
	private String department;
	private String role;
	private Long teamId;
	private String teamName;
	private String teamLeadName;
	private String managerName;
	private Integer projectId;
	private String projectName;
	private String projectManagerName;
	private List<String> projectMangerNameList;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Long isEmployeeActive;

	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String department, String role,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, Long isEmployeeActive) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		this.department = department;
		this.role = role;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.isEmployeeActive = isEmployeeActive;
	}

//	long, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.util.Date, java.time.LocalDateTime
	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String department, String role,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, LocalDateTime startDate, LocalDateTime endDate) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		this.department = department;
		this.role = role;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.startDate = startDate;

		this.endDate = endDate;
	}

}
