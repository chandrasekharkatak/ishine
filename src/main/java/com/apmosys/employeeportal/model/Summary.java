package com.apmosys.employeeportal.model;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Summary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long quarter;
	
	private Long empId;
	
	private Long goalsCompleted;
	
	private Long goalsRemaining;
	
	private Long kraKpiScore;
	
	private Long questionnaireScore;
	
}
