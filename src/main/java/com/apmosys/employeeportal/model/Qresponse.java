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

public class Qresponse {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reponseId;
	
	
	private Long id;
	private String questionText;
	private Float response;
	private Long empId;
	private Long quarterId;

}
