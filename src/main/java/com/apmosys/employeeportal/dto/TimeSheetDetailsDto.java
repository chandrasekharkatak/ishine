package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimeSheetDetailsDto {
	
	private Long timesheetId;
	private Long teamId;
	private Long empId;
	private String dayType;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;
}
