package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProjectInfoDTO {

	private Integer projectId;
	private String poNo;
	private String endDate;
	private Long id;
	private String startDate;
	private String projectName;
	private String projectType;
}
