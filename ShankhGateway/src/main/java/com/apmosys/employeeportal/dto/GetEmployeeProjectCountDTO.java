package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetEmployeeProjectCountDTO {

	private Long fixedCost;
	private Long tnm;
	private Long shadow;
	private Long bench;
	private Long internalRNDProducts;
	private Long total;
	
}
