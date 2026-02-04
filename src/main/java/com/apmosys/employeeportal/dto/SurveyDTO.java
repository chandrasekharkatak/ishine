package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyDTO {

	private Long empId;
	private Long surveyId;
	private String surveyName;
	private String isActive;
	private String description;
	private String createdOn;
	private Long createdBy;
	private String updatedOn;
	private Long updatedBy;
	private List<SurveyQuestionDTO> surveyQuestionList;
	private String createdByName;
	private String updatedByName;
	private String type;
	private Long employeementId;
	
	private String imageUrl;
    private String videoUrl;
    
    // Training Quiz Mapping fields (for quiz creation from training)
    private Integer trainingId;
    private Integer contentId;
    private Boolean isMandatory;
    private Boolean mustPassToComplete;
	
}
