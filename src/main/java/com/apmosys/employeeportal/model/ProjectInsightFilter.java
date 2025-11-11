package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class ProjectInsightFilter {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long filterId;
	private String filterName;
	private Integer projectId;

}
