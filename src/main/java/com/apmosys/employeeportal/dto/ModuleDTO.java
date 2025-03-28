package com.apmosys.employeeportal.dto;
import java.util.List;

import lombok.Data;

@Data
public class ModuleDTO {

	private Long milestoneId;
    private Long moduleId;
    private String module;
	private String description;
    private Long assignedTo;
	private Long redmineId;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;	
	private Long updatedBy;
	
	private List<ProjectQuestionDTO> projectQuestion;
	private List<SubModuleDTO> subModuleList;
	
}
