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
	    
	    private List<ProjectFetchDTO> expiredTNMProjects;
	    private List<ProjectFetchDTO> activeTNMProjects;
	    
	    private List<ProjectFetchDTO> monitoringProjects;
	    private List<ProjectFetchDTO> internalProjects;
	    //unfilledPositions
	    private List<ProjectFetchDTO> unfilledPositions;

	    
	    private List<ProjectFetchDTO> expiredTNMProjectsWithin1Month;
	    private List<ProjectFetchDTO> expiredTNMProjects1To2Months;
	    private List<ProjectFetchDTO> expiredTNMProjects2To3Months;
	    private List<ProjectFetchDTO> expiredTNMProjects3To6Months;
	    private List<ProjectFetchDTO> expiredTNMProjects6To9Months;
	    private List<ProjectFetchDTO> expiredTNMProjects9To12Months;
	    private List<ProjectFetchDTO> expiredTNMProjectsAbove12Months;
	    private List<ProjectFetchDTO> allExpiredTNMProjects;

}
