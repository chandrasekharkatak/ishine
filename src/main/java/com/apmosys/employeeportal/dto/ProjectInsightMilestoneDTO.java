package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightMilestoneDTO {

	private Long empId;
	private String employeeRole;
	private String milestone;
	private Long milestoneId;
	private String description;
	private Long redmineId;
	private List<Long> assignedToUserId;
	private String assignedToUserNames;
	private List<Long> taggedToUserId;
	private String taggedToUserNames;
	private List<ProjectQuestionDTO> questionList;
	private List<ModuleDTO> moduleList;
	private String performanceTabName;
	private Long deptId;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;
	private Long updatedBy;
	private Long projectId;
	private String actionType;

}
