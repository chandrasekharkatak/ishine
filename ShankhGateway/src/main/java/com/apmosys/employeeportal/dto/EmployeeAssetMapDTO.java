package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeAssetMapDTO {

	private String departmentName;
	private Long assetId;
	private String assestName;
	private String isAssigned;
	private Long empId;
	
}
