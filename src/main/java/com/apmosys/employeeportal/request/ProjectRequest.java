package com.apmosys.employeeportal.request;

import com.apmosys.employeeportal.dto.ProjectFilterDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {

	private String tabName;
	
	
	 @JsonProperty("projectFilterDTO") 
	private ProjectFilterDTO projectFilter;
}
