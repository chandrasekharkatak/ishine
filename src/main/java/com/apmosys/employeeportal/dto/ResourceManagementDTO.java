package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResourceManagementDTO {

	private Long draftTeamId;
	private Long poProjectId;
	private String projectName;
	private String teamName;
	private String description;
	private Long teamLeadId;
	private List<TeamDTO> teamList;
	
	private Long createdBy;
	private Long updatedBy;
	
}
