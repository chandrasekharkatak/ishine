package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

@Getter
@Setter
public class ReviewTableDto {
	private Long employeeId;
	private Long goalId;
	private Long kpiId;
	private Long reviewerId;
	private Integer quarterId;
	private Float rating;
	private String remarks;
	
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate reviewDate;
	
	private Integer kpiScore;
	
}
