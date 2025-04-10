package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class SubModuleDTO {

	private String subModule;
	private Long submoduleId;
	private Long moduleId;
	private String description;
	private Long redmineId;
	private List<Long> assignedToUserId;
	private String assignedToUserNames;
	private List<Long> taggedToUserId;
	private String taggedToUserNames;
	private List<ProjectQuestionDTO> questionList;
	private List<SubModuleDTO> subSubModuleList;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;
	private Long updatedBy;
}
