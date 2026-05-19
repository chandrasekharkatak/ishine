package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightSubServiceDTO {
	
	private String subService;
    private List<ProjectInsightSubServiceDTO> subServiceChildren;

}
