package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SurveyQuestionDTO {

	private Long surveyQuestionId;
	private Long surveyId;
	private String question;
	private String optionType;
	private String options;
	private String required;
	private String description;
	private String response;
	private String createdOn;
	private String name;
	private Long employeementId;
	private Long responseId;
	 private String isApprentice;
	    private String isConsultant;
	    private String isApmosysProduct;
	    private String employmentIdAccToET;
	private Integer marksObtained;
	private String passStatus;
	private String correctAnswer;

}
