package com.apmosys.employeeportal.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetEmployeeDashboardCountDTO {
	
	private String poProjectType;
    private Map<String, Integer> perEmployeeBillability;
    private Map<String, Integer> perProjectBillability;
    private int totalEmployee;
    private int totalProject;

}
