package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class ApiSource {

	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	private Long apiSourceId;
	private String label;
	private String url;
	private String labelKey;
	private String valueKey;
	private String isdependent;
	
}
