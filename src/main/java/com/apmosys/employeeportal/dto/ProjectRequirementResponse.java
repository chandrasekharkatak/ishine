package com.apmosys.employeeportal.dto;



import java.util.List;

import com.apmosys.employeeportal.dto.ProjectRequirementsDTO;
import com.apmosys.employeeportal.response.ResourceRequirementResponse;

import lombok.Data;


@Data
public class ProjectRequirementResponse {
	 private List<ResourceRequirementResponse> resourceRequirementList;
	 private ProjectRequirementsDTO resourceRequirements;
}
