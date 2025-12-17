package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetProjectListForDateAndEmpIdPayload {
	
	private Long empId;
	private LocalDateTime date;
	private Long projectId;

}
