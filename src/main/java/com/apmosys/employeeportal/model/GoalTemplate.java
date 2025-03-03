package com.apmosys.employeeportal.model;

//import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Entity
@Getter
@Setter
public class GoalTemplate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tempateId;
	
	private String title;
	
	private String description;
	
	private Long createById;
	
	private String createdByName;
	
	private String department;
	
	private Long approvedById;
	
	private String approvedByName;
	
	private Boolean isApproved;
}
