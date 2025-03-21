package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightDTO {

	private Long questionId;
    private Long empId;
    private String description;
    private String createdOn;
    private Long createdBy;
    private String updatedOn;
    private Long updatedBy;

    private String projectName;
    private String createdByName;
    
    private Long projectId;
    private Long projectManagerId;
    private String projectManagerName;

    private List<ProjectInsightQuestionDTO> projectInsightQuestionList;
    private String projectInsightQuestionTemplate;
	
}
