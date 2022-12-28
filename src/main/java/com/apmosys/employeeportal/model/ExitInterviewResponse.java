package com.apmosys.employeeportal.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ExitInterviewResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long responseId;
	private String question;
	private String response;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
}
