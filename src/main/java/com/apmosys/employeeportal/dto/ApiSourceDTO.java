package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiSourceDTO {
	
	private Long apiSourceId;
	private String label;
	private String url;
	private String labelKey;
	private String valueKey;
	private String isdependent;
	
	

}
