package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResourceRequirementDTO {

	private String role;
    private Integer count;
    private String experience;
    private String department;
    private Long resourceOverviewId;
	private Integer projectId;
    
}