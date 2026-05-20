package com.apmosys.employeeportal.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="HRPolicies")
public class UploadPolicy {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long policyID;
	
	private String policyName;
	
	private String fileName;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();

	private String readEnabled;
	
	
	
}
