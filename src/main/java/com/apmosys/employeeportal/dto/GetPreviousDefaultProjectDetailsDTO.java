package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetPreviousDefaultProjectDetailsDTO {
	
	private Long empId;
	private String primaryProjectName;
	private String teamName;
	private String clientName;
	private String billableType;
	private String startDate;

}
