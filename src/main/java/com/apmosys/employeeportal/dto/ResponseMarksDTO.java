package com.apmosys.employeeportal.dto;

import lombok.Data;

@Data
public class ResponseMarksDTO {
	
	private Long empId;
	private Long questionId;
	private Double marks;
	private String markType;

}
