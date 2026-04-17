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
public class QuarterModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer quarterid;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate startdate;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate enddate;
}
