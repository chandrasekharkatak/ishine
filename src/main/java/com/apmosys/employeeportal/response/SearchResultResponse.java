package com.apmosys.employeeportal.response;

import java.util.List;

import com.apmosys.employeeportal.dto.ModuleDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsightMilestoneDTO;
import com.apmosys.employeeportal.dto.ProjectInsightUserContributionDTO;
import com.apmosys.employeeportal.dto.SubModuleDTO;

import lombok.Data;

@Data
public class SearchResultResponse {

	private List<ProjectInsightDTO> projectList;
	private List<ProjectInsightMilestoneDTO> projectInsightMilestoneList;
	private List<ModuleDTO> moduleList;
	private List<SubModuleDTO> subModuleList;
	private List<SubModuleDTO> subSubModuleList;
	private List<ProjectInsightUserContributionDTO> userContributionList;
	
	private ProjectInsightDTO project;
	private ProjectInsightMilestoneDTO milestone; 
	private ModuleDTO module;
	private SubModuleDTO subModule;
	private SubModuleDTO subSubModule;
	
}
