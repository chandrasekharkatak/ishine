package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ModuleDTO {

	private String module;
	private Long moduleId;
	private Long milestoneId;
	private String description;
	private Long redmineId;
	private List<Long> assignedToUserId;
	private String assignedToUserNames;
	private List<Long> taggedToUserId;
	private String taggedToUserNames;
	private List<ProjectQuestionDTO> questionList;
	private List<SubModuleDTO> subModuleList;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;
	private Long updatedBy;
	private String actionType;

}
