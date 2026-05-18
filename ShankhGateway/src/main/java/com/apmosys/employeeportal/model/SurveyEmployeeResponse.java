package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "survey_employee_response")
public class SurveyEmployeeResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyEmployeeResponseId;
	
	private Long surveyQuestionId;
	
	@Column(length = 1000)
	private String response;
	
	private Long empId;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
}
