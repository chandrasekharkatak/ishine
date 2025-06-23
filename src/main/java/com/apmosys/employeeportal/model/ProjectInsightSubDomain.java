package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ProjectInsightSubDomain {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subDomainId;
	private String subDomain;
	private Long domainId;
	private Long parentSubDomainId;

}
