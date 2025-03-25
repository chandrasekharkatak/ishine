package com.apmosys.employeeportal.model;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Kresponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long responseId;
	private Float response;
	private Long id;
	private Long empId;
	private Long quarterId;
	private String description;
	
}
