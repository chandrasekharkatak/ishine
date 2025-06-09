package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CombinedPOInternalProjectResponse {
	
	    private List<ResourceManagementDTO> combinedProjects;
	    private List<ProjectFetchDTO> combinedNewProjects;
	    private Map<String, Integer> counts;

}
