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
public class Help {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long helpDocId;
	
	private String helpDocumentName;
	
	private String fileName;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
}
