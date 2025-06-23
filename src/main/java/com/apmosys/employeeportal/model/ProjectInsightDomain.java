package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class ProjectInsightDomain {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long domainId;
	private String domain;

}
