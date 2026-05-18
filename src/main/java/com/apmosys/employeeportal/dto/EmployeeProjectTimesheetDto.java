package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeProjectTimesheetDto {

	private Long empId;
	private Long etmId;
	private Integer projectId;
	private String projectName;
	private String projectType;
	private Long teamId;
	private String teamName;
	private LocalDate projectStartDate;
	private LocalDate employeeTeamStartDate;
	private LocalDate employeeTeamEndDate;
	private Long timesheetFilledCount;
	private Long updatedBy;
	private boolean removePermanently;

	public EmployeeProjectTimesheetDto(Integer projectId, String projectName, String projectType, String teamName,
			Date projectStartDate, Date employeeTeamStartDate, Date employeeTeamEndDate, Long timesheetFilledCount) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.teamName = teamName;
		this.projectStartDate = projectStartDate != null ? LocalDate.parse(projectStartDate.toString()) : null;
		this.employeeTeamStartDate = employeeTeamStartDate != null ? LocalDate.parse(employeeTeamStartDate.toString())
				: null;
		this.employeeTeamEndDate = employeeTeamEndDate != null ? LocalDate.parse(employeeTeamEndDate.toString()) : null;
		this.timesheetFilledCount = timesheetFilledCount;
	}

	public EmployeeProjectTimesheetDto(Integer projectId, String projectName, String projectType, String teamName,
			Date projectStartDate, Date employeeTeamStartDate, Date employeeTeamEndDate, Long timesheetFilledCount,
			Long etmId) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.teamName = teamName;
		this.projectStartDate = projectStartDate != null ? LocalDate.parse(projectStartDate.toString()) : null;
		this.employeeTeamStartDate = employeeTeamStartDate != null ? LocalDate.parse(employeeTeamStartDate.toString())
				: null;
		this.employeeTeamEndDate = employeeTeamEndDate != null ? LocalDate.parse(employeeTeamEndDate.toString()) : null;
		this.timesheetFilledCount = timesheetFilledCount;
		this.etmId = etmId;
	}

	public EmployeeProjectTimesheetDto(Long empId, Long etmId, Integer projectId, String projectName,
			String projectType, Long teamId, String teamName,
			Date projectStartDate, Date employeeTeamStartDate, Date employeeTeamEndDate) {
		super();
		this.empId = empId;
		this.etmId = etmId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.teamId = teamId;
		this.teamName = teamName;
		this.projectStartDate = projectStartDate != null ? LocalDate.parse(projectStartDate.toString()) : null;
		this.employeeTeamStartDate = employeeTeamStartDate != null ? LocalDate.parse(employeeTeamStartDate.toString())
				: null;
		this.employeeTeamEndDate = employeeTeamEndDate != null ? LocalDate.parse(employeeTeamEndDate.toString()) : null;
	}

	public EmployeeProjectTimesheetDto(Integer projectId, String projectName, String projectType, String teamName,
			Date projectStartDate, Long timesheetFilledCount) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.teamName = teamName;
		this.projectStartDate = projectStartDate != null ? LocalDate.parse(projectStartDate.toString()) : null;
		this.timesheetFilledCount = timesheetFilledCount;
	}

}
