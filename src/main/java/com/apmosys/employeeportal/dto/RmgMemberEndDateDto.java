package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RmgMemberEndDateDto {

	private Long empId;
	private Long etmId;
	private LocalDateTime endDate;

}