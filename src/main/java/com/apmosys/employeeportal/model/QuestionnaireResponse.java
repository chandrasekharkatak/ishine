package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;


@Entity
@Getter
@Setter
public class QuestionnaireResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long responseId;//primary key

	private Long goalId;
	
	private Long empId;
    
	private Long questionId;//questtionnaireID
	

	
	private String questionTitle;//remove this

	private Long quarterId; 
	
	
	private String quarter;//remove quarter
	
	private String response;
	
	private String remarks;
	
	private Float score;
	
	}
