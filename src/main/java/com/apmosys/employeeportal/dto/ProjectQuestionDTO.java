package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ProjectQuestionDTO {
	
	private Long questionId;
    private String question;
    private String optionType;
    private String options;
    private String required;
    private String description;
    private String response;
    private String documentUpload;
    private Long milestoneId;
    
    private Long responseId;
    private Long entityId;
    private String entityType;
    private String documentPath;
    
    private String name;
	private Long employeementId;
	
	private Long empId;

//	private String optionsList;
}
