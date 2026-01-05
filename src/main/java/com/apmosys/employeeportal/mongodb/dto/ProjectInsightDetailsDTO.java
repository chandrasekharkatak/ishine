package com.apmosys.employeeportal.mongodb.dto;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

import lombok.Data;

@Data
public class ProjectInsightDetailsDTO {

	private ProjectInsightProjectDetails projectInsightProjectDetails;
	private ProjectInsightGroupDetails projectInsightGroupDetails;
	private ProjectInsightQuestionDetails projectInsightQuestionDetails;
	private ProjectInsightFormDetails projectInsightFormDetails;

}
