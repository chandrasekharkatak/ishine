package com.apmosys.employeeportal.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightDomainDTO {
	
	private String domain;
    private List<ProjectInsightSubDomainDTO> subDomainList;
    private List<ProjectInsightServiceDTO> serviceList;

}