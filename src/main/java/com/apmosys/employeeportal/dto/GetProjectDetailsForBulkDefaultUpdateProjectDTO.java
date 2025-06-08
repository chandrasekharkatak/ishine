package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class GetProjectDetailsForBulkDefaultUpdateProjectDTO {
	
	private Integer projectId;
    private String projectName;
    private List<GetProjectDetailsForBulkDefaultUpdateTeamDTO> teamList = new ArrayList<>();
    private List<ResourceRequirementDTO> resourceRequirement = new ArrayList<>();
    
}
