package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "Appraisal_Summary")
public class AppraisalSummary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private Long quaterId;
	
	private Long employeeId;
	
	private Float finalRating;
	
	private String finalRemarks;

	private Float appraisalScore;
	
	private String appraisalPercentage;

}
