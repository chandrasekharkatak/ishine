package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class biomaxFilter {
	
	private List<String> empId;
	private String startDate;
	private String endDate;
	private String viewtype;

}
