package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.*;

@Entity
@Getter
@Setter

public class AppraisalSummary {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private Long employeeId;
	
	private Integer QuarterId;
	
	private Float finalRating;
	
	private String finalRemarks;
	
	private Float appraisalScore;
	
	private String appraisalPercentage;
}
