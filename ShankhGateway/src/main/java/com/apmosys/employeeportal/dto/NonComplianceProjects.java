package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NonComplianceProjects {
	
	private Long empId;
	 private LocalDate fromDate;
	    private LocalDate toDate;

}
