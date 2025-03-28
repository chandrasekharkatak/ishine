package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightQuestionDTO {
	
	private Long milestoneId;
	private String milestone;
	private String description;
	private Long deptId;
    private List<ProjectQuestionDTO> projectQuestion;
    private List<ModuleDTO> moduleList;
    private Long assignedTo;

}
