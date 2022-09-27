package com.apmosys.employeeportal.model;

import javax.persistence.Embedded;
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
public class EmployeeDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeDocumentId;
	
	private String documentName;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
}
