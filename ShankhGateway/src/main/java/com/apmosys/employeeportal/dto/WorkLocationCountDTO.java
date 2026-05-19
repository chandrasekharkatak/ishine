package com.apmosys.employeeportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class WorkLocationCountDTO {
	
	private String clientLocation;
	private Long employeeCOUNT;
}
