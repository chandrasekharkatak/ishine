package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetMyReporteesTimesheetRequestsPayload {
	
	private Long empId;
	private Boolean clientFilter;

}
