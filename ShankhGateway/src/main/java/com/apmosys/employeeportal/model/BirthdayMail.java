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
public class BirthdayMail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long birthdayMailId;
	private String birthdayImage;
	private String description;
	private String heading;
	
}
