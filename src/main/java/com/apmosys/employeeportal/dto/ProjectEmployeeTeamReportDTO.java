package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEmployeeTeamReportDTO {

	private Integer projectId;
	private String projectName;
	private Long departmentId;
	private String departmentName;
	private Long employeementId;
	private String employeeName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;

	private Long resourceOverviewId;
	private Integer isShadow;
}
