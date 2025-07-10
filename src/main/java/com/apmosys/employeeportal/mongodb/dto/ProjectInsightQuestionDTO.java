package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProjectInsightQuestionDTO {
	
	private boolean required;
    private List<Object> responseList;
    private boolean documentUpload;
    private List<OptionValueDTO> optionsList;
    private List<Object> projectResponseList;
    private List<Object> taggedToUserId;
    private List<Object> toTagEmployeeList;
    private List<Object> toAssignEmployeeList;
    private List<BadgePathDTO> badgePathList;
    private String currentActiveBadgeLevel;
    private String text;
    private String description;
    private String optionType;

}
