package com.apmosys.employeeportal.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Entity
@Getter
@Setter
public class ReviewTable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer reviewerId;
	
	private Long employeeId;
	
	private Integer goalId;
	
	private Long kpiId;
	
	private Integer quarter;
	
	private Float rating;
	
	private String remarks;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate reviewDate;
	
	private Float kpiScore;
}
