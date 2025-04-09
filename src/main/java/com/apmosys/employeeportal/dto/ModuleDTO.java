package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ModuleDTO {

	private Long milestoneId;
	private Long moduleId;
	private String module;
	private String description;
	private List<Long> assignedTo;
	private Long redmineId;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;
	private Long updatedBy;
	private String assignedToUserNames;
	private String taggedToUserNames;
	private List<ProjectQuestionDTO> projectQuestion;
	private List<SubModuleDTO> subModuleList;
	 private List<Long> taggedForHelp;

}
