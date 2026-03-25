package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightServiceDTO {
	
	 private String service;
	 private List<ProjectInsightSubServiceDTO> subServiceList;

}
