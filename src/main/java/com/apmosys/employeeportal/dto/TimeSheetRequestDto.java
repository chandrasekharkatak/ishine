package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TimeSheetRequestDto {
	
	private Long teamId;
	private Long empId;
	@JsonFormat(pattern = "yyyy-MM-dd") 
	private LocalDate startDate;
	@JsonFormat(pattern = "yyyy-MM-dd") 
	private LocalDate endDate;
}
