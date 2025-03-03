package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
public class Questionnaire {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long goalId;

private Long questionId;

private String questionTitle;

private String questiondescription;

private String createdBy;

private Long quarterId;


	
}
