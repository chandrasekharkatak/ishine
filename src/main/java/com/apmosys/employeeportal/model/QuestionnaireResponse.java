package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;


@Entity
@Getter
@Setter
public class QuestionnaireResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long responseId;
	
 
	private Long goalId;
	
	private Long empId;
	
    
	private Long questionId;
	
	private String questionTitle;

	
	private String response;
	
	private String remarks;
	
	
	
	
}
