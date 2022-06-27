package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Holiday {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short holidayId;
	
	private String occasion;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate dateOfHoliday;
	
	private String dayOfTheWeek;
	
	private String optionalHoliday;
	
	private String customHoliday;
}
