package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QresponseDTO {
	
	private Long id;
	private String questionText;
	private Float response;
	private Long empId;
	private Long quarterId;
	
}
