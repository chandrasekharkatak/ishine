package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "survey_questions")
public class SurveyQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long surveyQuestionId;

	private Long surveyId;

	private String question;

	@Column(length = 20)
	private String optionType;

	@Column(length = 1000)
	private String options;

	@Column(columnDefinition = "varchar(10) DEFAULT 'N'")
	private String required;

	private String description;

}
