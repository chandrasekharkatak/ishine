package com.apmosys.employeeportal.dto;

import java.util.Date;
import java.time.LocalDate;

import lombok.Data;

@Data
public class EmployeeProjectTimesheetCountDto {

	private Integer projectId;
	private String projectName;
	private LocalDate projectStartDate;
	private LocalDate employeeTeamStartDate;
	private Long timesheetFilledCount;

	public EmployeeProjectTimesheetCountDto(Integer projectId, String projectName, Date projectStartDate,
			Date employeeTeamStartDate, Long timesheetFilledCount) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectStartDate = projectStartDate != null ? LocalDate.parse(projectStartDate.toString()) : null;
		this.employeeTeamStartDate = employeeTeamStartDate != null ? LocalDate.parse(employeeTeamStartDate.toString()) : null;
		this.timesheetFilledCount = timesheetFilledCount;
	}

}
