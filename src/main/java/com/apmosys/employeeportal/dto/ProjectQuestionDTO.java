package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectQuestionDTO {
	
	private Long questionId;
    private String question;
    private String optionType;
    private String options;
    private String required;
    private String description;
    private String documentUpload;
    private Long milestoneId;
    private Long responseId;
    private String response;
    private Long entityId;
    private String entityType;
    private String documentPath;
    private String name;
	private Long employeementId;
	private Long empId;
	private List<ProjectResponseDTO> projectResponseList;
	private boolean isTagged;
	private String taggedToUserNames;
    private List<Long> taggedForHelp;
	
}
