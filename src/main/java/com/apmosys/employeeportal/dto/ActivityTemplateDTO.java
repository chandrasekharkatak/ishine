package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ActivityTemplateDTO {

	private Long activityTemplateId;
	private List<ActivityDTO> templateActivityList;
	private Long deptId;
	private String employeeRole;
	private String departmentName;
	private String activityDescription;
	private String activity;
	
}
