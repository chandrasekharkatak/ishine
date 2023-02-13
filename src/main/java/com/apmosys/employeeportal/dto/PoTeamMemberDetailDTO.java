package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PoTeamMemberDetailDTO {
	
	private String empId;
	private String month;
	private String startDate;
	private String endDate;
	private Integer workignDays;
	
}
