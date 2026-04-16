package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class OptionDTO {
	
	private String label;
	private String value;

	private Boolean isChildAvailable = false;
	private String hierarchyType;

}
