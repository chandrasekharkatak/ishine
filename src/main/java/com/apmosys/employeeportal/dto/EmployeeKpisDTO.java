package com.apmosys.employeeportal.dto;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EmployeeKpisDTO {
	
    private Long id;
    private String description;
    private String review;
    private Long kpiMappingId; 
    private Long progress;
    
	
		
}
