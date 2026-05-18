package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDeleteValidationRowDto {
	private Long teamId;
	private Long empId;
	private String employeeCode;
	private String employeeName;
	private LocalDate teamStartDate;
	private String status;
}

