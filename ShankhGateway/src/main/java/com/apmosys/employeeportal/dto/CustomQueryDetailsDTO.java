package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomQueryDetailsDTO {
	
private Long customQueryId;
	
	private Long createdBy;
	
	private String queryName;
	
	private List<String> availableColumns;
	
	private List<String> selectedColumns;

}
