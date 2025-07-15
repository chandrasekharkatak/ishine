package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInsightSubDomainDTO {
	
	private String subdomain;
    private List<ProjectInsightSubDomainDTO> subDomainChildrenList;
    private List<ProjectInsightServiceDTO> serviceList;

}
