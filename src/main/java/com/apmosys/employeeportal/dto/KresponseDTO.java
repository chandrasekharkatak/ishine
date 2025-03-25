package com.apmosys.employeeportal.dto;
import lombok.*;


@Getter
@Setter
public class KresponseDTO {

	private Long id;
	private Long empId;
	private Long quarterId;
	private String description;
	private Float response;
	
}
