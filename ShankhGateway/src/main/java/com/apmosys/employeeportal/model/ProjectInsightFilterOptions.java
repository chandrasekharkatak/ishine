package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class ProjectInsightFilterOptions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long optionId;
	private String optionName;
	private Long filterId;
	
}
