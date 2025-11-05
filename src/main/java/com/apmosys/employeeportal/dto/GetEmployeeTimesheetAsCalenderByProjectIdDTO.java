package com.apmosys.employeeportal.dto;

import org.springframework.data.repository.query.Param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetEmployeeTimesheetAsCalenderByProjectIdDTO {
	
	private Integer projectId;
	private Integer month;
	private Integer year;
	private Long empId;

}
