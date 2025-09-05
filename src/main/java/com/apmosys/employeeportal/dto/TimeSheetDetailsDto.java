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
public class TimeSheetDetailsDto {
	
	private Long timesheet_id;
	private Integer project_id;
	private Long teamId;
	private Long emp_id;
	private String dayType;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate date;
	
	public TimeSheetDetailsDto(
	        Long timesheet_id,
	        Integer project_id,
	        Long teamId,
	        Long emp_id,
	        String dayType,
	        LocalDate date) {
	    this.timesheet_id = timesheet_id;
	    this.project_id = project_id;
	    this.teamId = teamId;
	    this.emp_id = emp_id;
	    this.dayType = dayType;
	    this.date = date;
	}

}